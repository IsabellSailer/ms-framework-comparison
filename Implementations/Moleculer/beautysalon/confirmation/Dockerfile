FROM node:current-alpine

RUN mkdir /app
WORKDIR /app

COPY package.json package-lock.json ./

RUN npm install --production

COPY moleculer.config.js confirmation.service.js ./
COPY docker.env ./local.env

CMD ["npm", "start"]
