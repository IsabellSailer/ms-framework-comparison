FROM jolielang/jolie:1.9.0

WORKDIR /usr/confirmation

COPY confirmation.ol /usr/confirmation/
COPY public /usr/confirmation/public

EXPOSE 8082

ENTRYPOINT ["jolie", "confirmation.ol"]