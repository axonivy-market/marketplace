# # Stage 1: Build Angular app
# FROM node:20-alpine AS build-angular
# ARG BUILD_ENV
# WORKDIR /app
# COPY . .
# RUN npm install
# RUN npm run build -- --configuration $BUILD_ENV

# # Stage 2: Serve Angular app using Nginx
# FROM nginx
# # COPY --from=build-angular /app/dist/browser /usr/share/nginx/html
# COPY --from=build-angular /app/dist/server /usr/share/nginx/html

# # Change ownership of the shared volume
# RUN mkdir -p /usr/share/nginx/html/market-cache
# RUN chown -R www-data:www-data /usr/share/nginx/html/market-cache
# RUN chmod -R 755 /usr/share/nginx/html/market-cache
# RUN chmod g+s /usr/share/nginx/html/market-cache


# Stage 1: Build Angular SSR app
FROM node:20-alpine AS build-angular
# ARG BUILD_ENV
WORKDIR /app
COPY . .

# Install dependencies
RUN npm install

# Build both browser and server bundles
RUN npm run build -- --configuration $BUILD_ENV

# Stage 2: Serve with Node.js
FROM node:20-alpine AS runtime
WORKDIR /app

# Copy only the needed files from the build
COPY --from=build-angular /app/dist /app/dist
COPY --from=build-angular /app/package.json /app/package.json

# Install only production dependencies
RUN npm install --omit=dev

# Expose the SSR server port
EXPOSE 4000

# Start the Angular SSR server
CMD ["node", "dist/server/server.mjs"]