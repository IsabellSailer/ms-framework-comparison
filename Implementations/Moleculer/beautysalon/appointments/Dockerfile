FROM node:current-alpine

ENV NODE_ENV=production
ENV POSTGRES_HOST="appointments-db"
ENV POSTGRES_USER="postgres"
ENV POSTGRES_PASSWORD="docker"
ENV SERVICES=appointments,api
ENV LOGGER=true
ENV LOGLEVEL=info
ENV TRANSPORTER=amqp://rabbitmq:5672

RUN mkdir /app
WORKDIR /app

COPY package.json package-lock.json ./

RUN npm install --production

COPY moleculer.config.js appointments.service.js api.service.js ./

EXPOSE 8081:8081

CMD ["npm", "start"]
