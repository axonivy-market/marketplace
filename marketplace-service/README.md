# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.2.5/maven-plugin/reference/html/)
* [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.2.5/reference/htmlsingle/index.html#web)

### Guides

The following guides illustrate how to use some features concretely:

* Installing postgresql, and access it as Url jdbc:postgresql://localhost:5432/marketplace-service, and you can create
  and name whatever you want,then you should put them to application.properties
* You can change the postgreSQL configuration in file `application.properties`
    ```
    spring.datasource.url=${POSTGRES_HOST_URL}
    spring.datasource.username=${POSTGRES_USERNAME}
    spring.datasource.password=${POSTGRES_PASSWORD}
    spring.jpa.show-sql=true/false
    spring.datasource.driver-class-name=org.postgresql.Driver
    ```
* Update GitHub token in file `github.token`
* Run mvn clean install to build project
* Run mvn test to test all tests

### Access Swagger URL: http://{your-host}/swagger-ui/index.html

### Scheduled Task Monitoring Endpoint

The service now exposes runtime information for all `@Scheduled` tasks.

Endpoint: `GET /api/scheduled-tasks`

Returned fields per task:
* `id` - Identifier in form `ClassName#methodName`
* `cronExpression` - Resolved cron string (if configured)
* `lastStart` / `lastEnd` - Timestamps of the most recent execution
* `lastSuccessEnd` - Timestamp when the last successful run finished
* `nextExecution` - Next expected run time (cron only)
* `running` - Whether the task is currently executing
* `lastSuccess` - Success flag for the last execution
* `lastError` - Error message of the last failure (if any)

Example response:
```json
[
  {
    "id": "ScheduledTasks#syncDataForProductFromGitHubRepo",
    "cronExpression": "0 0/30 * * * *",
    "lastStart": "2025-11-20T09:00:00Z",
    "lastEnd": "2025-11-20T09:00:12Z",
    "lastSuccessEnd": "2025-11-20T09:00:12Z",
    "nextExecution": "2025-11-20T09:30:00Z",
    "running": false,
    "lastSuccess": true,
    "lastError": null
  }
]
```

You can integrate this endpoint with monitoring dashboards or periodically poll it to verify scheduler health.

### Install Lombok for Eclipse IDE

* Download lombok here https://projectlombok.org/download
* run command "java -jar lombok.jar" then you can access file “eclipse.ini“ in eclipse folder where you install → there
  is a text like this:  -javaagent:C:\Users\tvtphuc\eclipse\jee-2024-032\eclipse\lombok.jar → it means you are
  successful
* Start eclipse
* Import the project then in the eclipse , you should run the command “mvn clean install“
* After that you go to class MarketplaceServiceApplication → right click to main method → click run as → choose Java
  Application
* Then you can send a request in postman
* If you want to run single test in class UserServiceImplTest. You can right-click to method testFindAllUser and right
  click → select Run as → choose JUnit Test