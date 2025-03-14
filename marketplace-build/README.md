# Get starts with Marketplace build

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
