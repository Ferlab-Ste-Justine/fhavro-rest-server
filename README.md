# fhavro-rest-server

An example using FHAVRO library embedded in a REST server

## What is the goal of this project ?

The main goal of this project is to ease the interaction with the FHAVRO library (https://github.com/Ferlab-Ste-Justine/fhavro) by 
providing REST endpoints to test the functionality of the library.

## What is this project using ?

This project uses the Spring Framework, more specifically Spring Boot.
Moreover, it leverages SpringFox for its Swagger to simplify the REST interaction.
Below is a table with the business version of the technology used for this project.

Java | Maven | Spring | Swagger | Fhir | Avro 
--- | --- | --- | --- | --- | ---
11 | 4.0.0 | 2.5.5 | 2.0 | v4.0.1 | 1.10.2

## How to contribute ?

Simply fork this project and clone into your local repository and open it as a Maven project in your favorite IDE.

## How to run ?

The main class is located at src/main/java/bio/ferlab/fhir/app/Application
In the project hierarchy, right-click Application and run Application.main();

After Spring boot initialization, a Swagger will be served at: http://localhost:8080/swagger-ui.html

## What is this data ?

This REST API leverages the public FHIR server UHN_HAPI Server (R4 FHIR): https://hapi.fhir.org/

## Known limitation

As of right now, the library is not published unto the Maven repository and therefore you will have to 
package and build your own jar to be manually included in this library, but we are working on it.

## Future development

- Allow generating custom schemas based on provided Profiles and Extensions