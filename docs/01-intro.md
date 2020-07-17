# Introduction - Development

This project is split into 4 different projects, each being a Maven module
that can be used as a dependency. The projects are split like this:

* `karuta-model` provides common objects like JPA entities and Jackson
   document classes.
* `karuta-consumer` provides the interfaces that represent Spring Data
   repositories to read or write data to the database.
* `karuta-business` provides the service layer with all the working logic
   of the application.
* `karuta-webapp` is the Spring Boot application that actually provides
  the back-end application.

## Setting up the environment

To work on this project, you will need :

* Java 8+
* MySQL 5.7+
* Maven
* Git

Once this repository is cloned, you can import it in your favorite IDE.

## Running tests

See the [Tests](tests.md) document for further information on the testing
aspects.

To fully package the different components and run the different tests,
you can direcrtly invoke:

~~~
$ mvn install
~~~

## Running the application

You can either start the Spring application from your IDE or, from a
terminal, located in the root directory of this repository, run:

~~~
$ mvn spring-boot:run -pl karuta-webapp
~~~

You can customize the database settings under the `application.properties`
file located in the `karuta-webapp/src/main/resources` folder.
