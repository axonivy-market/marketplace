# Get start

### Docker compose for DEV environment
* Navigate to ``marketplace-service`` and execute maven build for springboot app to get the war file:
  ```
    mvn clean install -DskipTests
  ```

* Navigate to ``marketplace-ui`` and execute node install and run angular app to get the dist folder:
  ```
    npm instal
    npm run build --prod
  ```

* Please run ``docker-compose up --build`` from folder ``marketplace-build`` to start a Marketplace DEV at local

### Docker compose for PROD deployment
* Navigate to ``marketplace-build/release`` and run ``docker-compose up`` to clone the docker images from GitHub packages and run the website