# Karuta

This repository contains the source code of the Karuta backend. This project
provides the logic layer of the Karuta project ; to fully set it up, you also
need to install :

* [Karuta frontend](https://github.com/karutaproject/karuta-frontend)
* [Karuta fileserver](https://github.com/karutaproject/karuta-fileserver)

## Installation

To install this project, you need the following requirements:

* MySQL 5 or 8
* Java 8+
* Tomcat

Then, you need to do the following steps:

* Download the WAR archive and put it in the `webapps/` folder of
  your Tomcat installation.
* Create a MySQL database
* Then, configure the connection to the database by placing a file
  called `application.properties` inside the `lib/config` directory
  of Tomcat.
  
  You can use [this one](karuta-webapp/src/main/resources/application.properties)
  as a basis.

## Additional documentation

If you are looking for the documentation of the different available
endpoints, once the application is started, you can go to the
`/rest/api/docs/index.html` URL.

On the other hand, here are some additional documentation for developers:

* [Introduction](docs/01-intro.md)
* [Security](docs/02-security.md)
* [Tests](docs/03-tests.md)

## License

Karuta is distributed under the terms of the ECL (Educational Community
License) version 2.0. You can check out the `LICENSE` file for further
information.