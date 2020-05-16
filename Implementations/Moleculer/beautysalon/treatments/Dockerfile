FROM node:current-alpine

RUN mkdir /app
WORKDIR /app

COPY package.json package-lock.json ./

RUN npm install --production

COPY moleculer.config.js treatments.service.js api.service.js ./
COPY docker.env ./local.env

EXPOSE 8080:8080

CMD ["npm", "start"]
