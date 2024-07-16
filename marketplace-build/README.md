# Get starts with Marketplace build

### Set up MongoDB with authentication mode
* Navigate to ``marketplace-build/config/mongodb`` and execute the ``docker-compose up`` to start MongoDB with non-auth mode
* Create root user for authentication
  ```
  use admin
  db.createUser(
    {
      user: "username",
      pwd: "password",
      roles: [
        { role: "userAdminAnyDatabase", db: "admin" },
        { role: "readWriteAnyDatabase", db: "admin" }
      ]
    }
  )

  db.grantRolesToUser('username', [{ role: 'root', db: 'admin' }])
  ```

* [Optional] Execute authentication test for the created user
  ```
  use admin
  db.auth('username','password')
  ```
This command should return the ``OK`` code

### Docker build for DEV environment
* Navigate to ``marketplace-service`` and execute maven build for spring-boot app to get the war file:
  ```
    mvn clean install -DskipTests
  ```

* Navigate to ``marketplace-ui`` and execute node install and run the angular app to get the dist folder:
  ```
    npm instal
    npm run build --prod
  ```

* Please run ``docker-compose up --build`` from folder ``marketplace-build`` to start a Marketplace DEV at the local

### Docker release
To release a new version for marketplace images, please trigger the ``Docker Release`` actions.
* This GH Actions will trigger the ``Docker build`` on the master branch.
* Login to GitHub Registry Hub.
* Deploy new image to packages.
Please verify the result in the ``Package`` after the build is completed.

### Docker compose for PROD deployment
* Navigate to ``marketplace-build/release`` run ``docker-compose up`` to clone the docker images from GitHub packages and start the website
