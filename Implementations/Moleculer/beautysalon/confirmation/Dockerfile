FROM node:current-alpine

ENV NODE_ENV=production
ENV SERVICES=confirmation
ENV LOGGER=true
ENV LOGLEVEL=info
ENV TRANSPORTER=amqp://rabbitmq:5672

RUN mkdir /app
WORKDIR /app

COPY package.json package-lock.json ./

RUN npm install --production

COPY moleculer.config.js confirmation.service.js ./

CMD ["npm", "start"]
