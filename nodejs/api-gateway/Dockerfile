FROM node:7

RUN mkdir -p /home/node/app/node_modules && chown -R node:node /home/node/app

WORKDIR /home/node/app

COPY package*.json ./
COPY pdi-kettle-carte-hook-module ./
COPY --chown=node:node . .

USER node

WORKDIR /home/node/app
RUN npm install && npm i fast-gateway

EXPOSE 9191

CMD [ "node", "app.js" ]
#CMD ["bash"]