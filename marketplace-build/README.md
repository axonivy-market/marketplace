# Get starts with Marketplace build

### Set up MongoDB with authentication mode
* Navigate to ``marketplace-build/config/mongodb`` and execute the ``docker-compose -f non-authen-docker-compose.yml up -d`` to start MongoDB with non-auth mode and create a root admin user.

* [Optional] Execute authentication test for the created user
  ```
  use admin
  db.auth('username','password')
  ```
This command should return the ``OK`` code

* Down the non-authen instance to start the main docker compose file by run ``docker-compose down``

### Docker build for DEV environment
#### Start from scratch:
* Navigate to ``marketplace-build``

* Run ``docker-compose up -d --build`` to start a Marketplace DEV at the local

#### If you already have MongoDB on your local machine with public port `27017`
* Navigate to ``marketplace-build/dev``

* Run ``docker-compose up -d --build`` to start a Marketplace DEV at the local

> In case you want to set up the MongoDB as a standalone compose. Please run `docker-compose -f authen-docker-compose.yml up -d` in ``marketplace-build/config/mongodb``

### Docker release
To release a new version for marketplace images, please trigger the ``Docker Release`` actions.
* This GH Actions will trigger the ``Docker build`` on the master branch.
* Login to GitHub Registry Hub.
* Deploy new image to packages.
Please verify the result in the ``Package`` after the build is completed.

### Start Docker compose for PROD deployment
* Navigate to ``marketplace-build/release`` run ``docker-compose up -d`` to clone the docker images from GitHub packages and start the website

### Start Docker compose using SPRINT release version
* Navigate to ``marketplace-build/release`` run ``docker-compose -f sprint-compose.yml up`` to clone the docker images from GitHub packages and start the website


