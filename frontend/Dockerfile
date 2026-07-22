FROM node:20-alpine

WORKDIR /app

COPY package.json ./
RUN npm install

COPY . .

EXPOSE 4200

CMD ["sh", "-c", "npm run start -- --host 0.0.0.0 --port ${FRONTEND_PORT:-4200}"]
