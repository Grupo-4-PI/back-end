FROM node:20-slim

WORKDIR /usr/src/app

RUN apt-get update && apt-get install -y git

RUN git clone https://github.com/Grupo-4-PI/front-end.git

WORKDIR /usr/src/app/front-end/web-data-viz

RUN npm install

EXPOSE 3333

CMD [ "npm", "start" ]