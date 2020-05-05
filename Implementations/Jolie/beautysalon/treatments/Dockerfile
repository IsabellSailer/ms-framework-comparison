FROM jolielang/jolie:1.9.0

WORKDIR /usr/treatments

COPY jester_config.iol rest_template.json treatment.ol TREATMENTS.json /usr/treatments/
COPY config-docker.json /usr/treatments/config.json
COPY lib /usr/treatments/lib/
COPY public /usr/treatments/public

EXPOSE 8080

ENTRYPOINT ["jolier", "treatment.ol", "TREATMENTS"]
CMD ["localhost:8080"]