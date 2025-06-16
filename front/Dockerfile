FROM node:latest

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

RUN npm run build

RUN npm install -g http-server

EXPOSE 8081

CMD ["http-server", "dist"]
