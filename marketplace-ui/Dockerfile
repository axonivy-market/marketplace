# Stage 1: Build Angular app
FROM node:20-alpine AS build-angular
ARG BUILD_ENV
WORKDIR /app
COPY . .
RUN npm install
RUN npm run build -- --configuration $BUILD_ENV

# Stage 2: Serve Angular app using Nginx
FROM nginx
COPY --from=build-angular /app/dist/browser /usr/share/nginx/html
COPY --from=assets config/nginx/nginx.conf /etc/nginx/nginx.conf
