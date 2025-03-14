# Get starts with Marketplace build

### Setup PostgreSQL 
* Run ``docker pull postgres`` to pull the latest PostgreSQL image and execute ``docker volume create marketplace_service_data`` to create a named volume to persist PostgreSQL data

* Then start a PostgreSQL container with the created volume 
``docker run --name postgres_container \
              -e POSTGRES_USER=${POSTGRES_USERNAME} \
              -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
              -e POSTGRES_DB=${MARKETPLACE_DB} \
              -d -p 5432:5432 \
              -v marketplace_service_data:/var/lib/postgresql/data postgres``

Remember to replace *${POSTGRES_USERNAME}*, *${POSTGRES_PASSWORD}* with the credentials specified in [`marketplace-build/docker-compose.yml`](marketplace-build/docker-compose.yml). Additionally, set *${MARKETPLACE_DB}* to the database name specified in *${POSTGRES_HOST_URL}* within the same file.

### Docker build for local environment
#### Update the PostgreSQL configuration for env
* Navigate to ``marketplace-build/dev`` and edit ``.env`` base on your postgreSQL configuration

* Navigate to ``marketplace-build/dev``

* Run ``docker-compose up -d --build`` to start a Marketplace DEV at the local

### Docker release
To release a new version for marketplace images, please trigger the ``Docker Release`` actions.
* This GH Actions will trigger the ``Docker build`` on the master branch.
* Login to GitHub Registry Hub.
* Deploy new image to packages.
Please verify the result in the ``Package`` after the build is completed.

### Start Docker compose for PROD/SPRINT deployment
* Navigate to ``marketplace-build/release`` run ``docker-compose up -d`` to clone the docker images from GitHub packages and start the website
