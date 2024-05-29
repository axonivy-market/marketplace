# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.2.5/maven-plugin/reference/html/)
* [Spring Data MongoDB](https://docs.spring.io/spring-boot/docs/3.2.5/reference/htmlsingle/index.html#data.nosql.mongodb)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.2.5/reference/htmlsingle/index.html#web)

### Guides
The following guides illustrate how to use some features concretely:

* installing mongodb , and access it mongodb://localhost:27017/ in mongodb compass or studio3T
* run "mvn clean install" to build a project
* run "mvn test" to test all Test class

### MongoDB's property configs
* We can set up properties in class application.properties and MongoConfig

### Access Swagger URL: http://{your-host}/swagger-ui/index.html

### Steps to set up:
* Installing mongodb, and access it as Url mongodb://localhost:27017/, and you can create and name whatever you want ,then you should put them to application.properties
* Run mvn clean install to build project
* Run mvn test to test all tests
* You can change the configuration in file “application.properties“

### In case of using eclipse you should install manually Lombok .
* Download lombok here Download
* run command java -jar lombok.jar and restart the eclipse then you can access file “eclipse.ini“ in eclipse folder where you install → there is a text like this:  -javaagent:C:\Users\tvtphuc\eclipse\jee-2024-032\eclipse\lombok.jar → it means you are successful
* Import the project then in the eclipse , you should run the command “mvn clean install“
* After that you go to class MarketplaceServiceApplication → right click to main method → click run as → choose Java Application
* Then you can send a request in postman :)
* If you want to run single test in class UserServiceImplTest. You can right-click to method testFindAllUser and right click → select Run as → choose JUnit Test