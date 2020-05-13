FROM node:current-alpine

ENV NODE_ENV=production
ENV MONGO_URI="mongodb://treatments-db:27017/beautysalon"
ENV MONGO_USER=admin
ENV MONGO_PASSWORD=docker
ENV SERVICES=treatments,api
ENV LOGGER=true
ENV LOGLEVEL=info
ENV TRANSPORTER=amqp://rabbitmq:5672

RUN mkdir /app
WORKDIR /app

COPY package.json package-lock.json ./

RUN npm install --production

COPY moleculer.config.js treatments.service.js api.service.js ./

EXPOSE 8080:8080

CMD ["npm", "start"]
