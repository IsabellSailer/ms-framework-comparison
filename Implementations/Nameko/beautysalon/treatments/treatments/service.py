import logging
import json

from nameko.web.handlers import http
from nameko_tracer import Tracer
from redis import ConnectionError

from treatments import dependencies

logger = logging.getLogger(__name__)


class TreatmentsService:
    """
    This Service is responsible for the management of treatments.
    """

    name = 'treatments'

    storage = dependencies.Storage()
    tracer = Tracer()


    @http("GET", "/treatments/<int:treatment_id>")
    def get_treatment(self, request, treatment_id):
        """
        Returns all Information for the reqeusted Treatment
        :param request: http request
        :param treatment_id: the ID of a Treatment
        :return: 200 Details for the requested Treatment
        :return: 404 No Treatment for given ID
        :return: 500 Connection Error to Database 
        """
        try:
            treatment = self.storage.get(treatment_id)
        except KeyError:
            return 404, "No Treatment exists for the given ID.\n"
        except ConnectionError:
            return 500, "Could not read Treatment.\n"

        return 200, str(treatment)


    @http("GET", "/treatments/list")
    def list_treatments(self, request):
        """
        Shows a list of all offered Treatments
        :param request: http request
        :return: 200 List of all offered Treatments
        :return: 404 No stored Treatments 
        :return: 500 Connection Error to Database 
        """
        try:
            treatments = self.storage.list()

            formattedList = "\n"
            for t in treatments:
                formattedList += t['name'] + "\nID: " + str(t['id']) + "\nPrice: " + str(
                    t['price']) + " Euro\nMinimum Duration: " + str(
                    t['minduration']) + " hour(s)\nMaximum Duration: " + str(t['maxduration']) + " hour(s)\n\n"

            if formattedList != "\n":
                return 200, u"Available Treatments:\n {}".format(formattedList)
            else:
                return 404, "No Treatments exist.\n"
   
        except ConnectionError:
            return 500, "Could not read Treatments.\n"


    @http("POST", "/treatments/")
    def create_treatment(self, request):
        """
        Saves a new Treatment
        :param request: new Treatment that should be saved
        :return: 201 Detailed information for newly created Treatment
        :return: 500 Connection Error to Database 
        """
        treatment_data = json.loads(request.get_data())
        try:
            self.storage.create(treatment_data)
        except ConnectionError:
            return 500, "Could not create Treatment.\n"

        return 201, u"Treatment successfully saved: {}\n".format(treatment_data)
