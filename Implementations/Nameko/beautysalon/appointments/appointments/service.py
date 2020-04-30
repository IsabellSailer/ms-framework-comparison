import logging
import json
import requests
from ast import literal_eval
from sqlalchemy import exc

from nameko.web.handlers import http
from nameko_sqlalchemy import Database
from nameko.events import EventDispatcher
from nameko_statsd import StatsD
from nameko_tracer import Tracer
#from nameko_openapi import OpenApi

from appointments.models import Appointment, DeclarativeBase

logger = logging.getLogger(__name__)


class AppointmentsService:
    """
    This Service is responsible for the management of appointments.
    """

    name = 'appointments'
    # api = OpenApi('appointments.yaml')

    db = Database(DeclarativeBase)
    dispatch = EventDispatcher()
    #statsd = StatsD('prod')
    tracer = Tracer()

    # @api.operation('get_appointment')
    # @statsd.timer('get_appointment')
    @http("GET", "/appointments/<int:appointment_id>")
    def get_appointment(self, request, appointment_id):
        """
        Returns all Information for the reqeusted Appointment
        :param request: http request
        :param appointment_id: the ID of a Appointment
        :return: 200 Details for the requested Appointment
        :return: 404 No Appointment for given ID
        :return: 500 Connection Error to Database 
        """
        try:
            appointment = self.db.session.query(Appointment).get(appointment_id)

            formatted_appointment = "\n\n"
            formatted_appointment += "Name: " + appointment.customer_name + "\nTreatment ID: " + str(
                appointment.treatment_id) + "\nTreatment Name: " + appointment.treatment_name + "\nDate: " + appointment.date.strftime("%d.%m.%Y") + "\nTime: " + str(
                appointment.start_time) + ":00 - " + str(appointment.end_time) + ":00\n\n"

            return 200, u"\nAppointment: {}".format(formatted_appointment)
        except AttributeError:
            return 404, "No Treatment exists for the given ID.\n"
        except exc.SQLAlchemyError:
            return 500, "Could not read Appointment.\n"

    # @api.operation('get_appointments')
    # @statsd.timer('get_appointments')
    @http("GET", "/appointments/list")
    def get_appointments(self, request):
        """
        Shows a list of all settled Appointments
        :param request: http request
        :return: 200 List of all booked Appointments
        :return: 404 No stored Appointments 
        :return: 500 Connection Error to Database 
        """
        try:
            appointments = self.db.session.query(Appointment).all()

            formatted_appointments = "\n\n"

            for appointment in appointments:
                formatted_appointments += "Name: " + appointment.customer_name + "\nTreatment ID: " + str(
                    appointment.treatment_id) + "\nTreatment Name: " + appointment.treatment_name + "\nDate: " + appointment.date.strftime("%d.%m.%Y") + "\nTime: " + str(
                    appointment.start_time) + ":00 - " + str(appointment.end_time) + ":00\n\n"

            if formatted_appointments == "\n\n":
                return 404, "No Appointments exist.\n"
            else:
                return 200, u"\nAppointment: {}".format(formatted_appointments)

        except exc.SQLAlchemyError:
            return 500, "Could not read Appointments.\n"

    # @api.operation('create_appointment')
    # @statsd.timer('create_appointment')
    @http("POST", "/appointments/")
    def create_appointment(self, request):
        """
        Saves a new Appointment
        :param request: new Appointment that should be saved
        :return: 201 Detailed information for newly created Appointment
        :return: 404 No Treatment for given TreatmentID 
        :return: 409 Appointment Details do not match the requirements
        :return: 500 Connection Error  
        """

        appointment_detail = json.loads(request.get_data())

        if appointment_detail['start_time'] >= appointment_detail['end_time']:
            return 409, "Start time has to be before end time.\n"
        elif appointment_detail['start_time'] < 8 or appointment_detail['start_time'] > 17 or appointment_detail[
            'end_time'] < 9 or appointment_detail['end_time'] > 18:
            return 409, "Appointments are only available from 8 - 18. Please choose another timeslot.\n"

        try:
            URL = "http://treatments:8080/treatments/" + str(appointment_detail['treatment_id'])
            t_response = requests.get(url=URL)
            t_response.encoding = 'utf-8'
            treatment_data = literal_eval(t_response.text)
        except SyntaxError:
            return 404, "No Treatment exists for given TreatmentID.\n"
        except requests.exceptions.ConnectionError:
            return 500, "Could not read Treatments.\n"

        if treatment_data['minduration'] > (appointment_detail['end_time'] - appointment_detail['start_time']):
            return 409, "An appointment for " + treatment_data['name'] + " takes at least " + str(treatment_data[
                                                                                                 'minduration']) + " hour(s). Please choose another timeslot.\n"
        elif treatment_data['maxduration'] < (appointment_detail['end_time'] - appointment_detail['start_time']):
            return 409, "An appointment for " + treatment_data['name'] + " takes maximum " + str(treatment_data[
                                                                                                'maxduration']) + " hour(s). Please choose another timeslot.\n"

        try:
            conflicts = 0;
            appointments = self.db.session.query(Appointment).all()
            for appointment in appointments:
                if treatment_data['name'] == appointment.treatment_name:
                    if appointment_detail['date'] == appointment.date.strftime("%Y-%m-%d"):
                        if not (appointment_detail['start_time'] <= appointment.start_time and appointment_detail[
                            'end_time'] <= appointment.start_time) and not (
                                appointment.end_time <= appointment_detail['end_time'] and appointment.end_time <=
                                appointment_detail['start_time']):
                            conflicts += 1
                            
            if conflicts == 0:
                newappointment = Appointment(
                    treatment_id=appointment_detail['treatment_id'],
                    treatment_name=treatment_data['name'],
                    customer_name=appointment_detail['customer_name'],
                    date=appointment_detail['date'],
                    start_time=appointment_detail['start_time'],
                    end_time=appointment_detail['end_time'],
                    duration=appointment_detail['end_time'] - appointment_detail['start_time']
                )

                with self.db.get_session() as session:
                    session.add(newappointment)

                appointment_detail['treatment_name'] = treatment_data['name']
                self.dispatch("booked_appointment", appointment_detail)

                return 201, u"\nAppointment: {}".format(appointment_detail)
            else:
                return 409, "There were " + str(conflicts) + " conflicts with other appointments. Please choose a free timeslot.\n"

        except exc.SQLAlchemyError:
            return 500, "Could not save Appointment.\n"
