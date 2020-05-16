FROM node:current-alpine

RUN mkdir /app
WORKDIR /app

COPY package.json package-lock.json ./

RUN npm install --production

COPY moleculer.config.js appointments.service.js api.service.js ./
COPY docker.env ./local.env

EXPOSE 8081:8081

CMD ["npm", "start"]
