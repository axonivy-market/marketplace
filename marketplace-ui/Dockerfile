# Stage 1: Build Angular app
FROM node:14-alpine AS build-angular
WORKDIR /app
COPY . .
RUN npm install
RUN npm run build --prod

# Stage 2: Serve Angular app using Nginx
FROM nginx:alpine
COPY --from=build-angular /app/dist/browser /usr/share/nginx/html
COPY --from=assets ./config/nginx/nginx.conf /etc/nginx/nginx.conf
