# Build Phase
FROM node:22-alpine AS build
ARG BUILD_ENV
WORKDIR /app
# Only copy files needed for npm install first (better caching)
COPY package*.json ./
# Install dependencies
RUN npm ci
# Copy the rest of the source code
COPY . .
RUN npm run build -- --configuration $BUILD_ENV

# Run Phase
FROM node:22-alpine
# Create app folder
WORKDIR /app
# Copy built dist and minimal needed files
COPY --from=build /app/dist /app/dist
COPY --from=build /app/package.json /app/package.json
COPY --from=build /app/node_modules /app/node_modules

EXPOSE 4000

# Start the SSR server
CMD ["node", "dist/server/server.mjs"]
