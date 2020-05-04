FROM jolielang/jolie:1.9.0

WORKDIR /usr/appointments

COPY jester_config.iol rest_template.json appointment.ol APPOINTMENTS.json /usr/appointments/
COPY lib /usr/appointments/lib/
COPY public /usr/appointments/public

EXPOSE 8081

ENTRYPOINT ["jolier", "appointment.ol", "APPOINTMENTS"]
CMD ["localhost:8081"]