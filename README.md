This is an attempt to port the original work https://github.com/ornl-epics/pvws to a Spring Boot-based web application. While the code is adapted to the web socket APIs of Spring Boot, most of the code is essentially unchanged.

Endpoints
=========


`HTTP`/`HTTPS`
------
`/pvws`

This shows a webpage with documentation and test utilities.

`pvws/info`

This returns JSON information with general server info.

`pvws/summary`

This returns JSON with a summary of all active web sockets.

`pvws/socket`

This returns JSON with details on all active web sockets and their PVs.

`pvws/pool`

This returns JSON with a listing of all PVs in the PV connection pool.

`pvws/pvget`

This returns a single value read from a PV in the same JSON format as a websocket message.

Takes the fully qualified PV address as the `name` parameter. 

`WS`/`WSS`
----------

`pvws/pv`

This is the main websocket connection endpoint. See `/pvws` for information on commands. 

Requirements
------------

To build: JDK 17, Maven 3.8+

To run: JRE 17

Build
=====

Executable jar
--------------

To build an executable Spring Boot jar (embedded Tomcat):

``>mvn clean install``

Output is ``pvws.jar`` in the ``target`` directory.

Tomcat war
----------

To build war for deployment in Tomcat container:

``>mvn -Pwar clean install``

Output is ``pvws.war`` in the ``target`` directory.

Run
===

Settings
--------

Bundled ``application.properties`` defines a few settings (e.g. default protocol). To define other settings or override
settings, one may create a file named exactly ``application.properties`` and edit as needed. This
file must be readable by the user account owning the application process.

**NOTE:** If "ca" (channel access) is selected as default protocol, associated settings (e.g. EPICS_CA_ADDR_LIST) **must**
be defined as environment variables.

Executable jar
--------------

Launch like so:

``>java -Dspring.config.location=file:/path/to/directory/ -jar /path/to/pvws.jar``

where ``/path/to/directory/`` is the directory holding ``application.properties``. Note that
this string **must** end in a slash ("/").

It is also possible to define properties on the command line, e.g. 

``>java -Dspring.config.location=file:/path/to/directory/ -DEPICS_PVA_ADDR_LIST=1.2.3.4 -jar /path/to/pvws.jar``

Combining ``application.properties`` with JVM options is possible.
Command line options will override definitions in ``application.properties``.

Tomcat war
----------

**NOTE:** Verified on Tomcat 9 only.

Copy ``pvws.war`` to ``TOMCAT_ROOT/webapps``. Set environment variable:

``>export JAVA_OPTS=-Dspring.config.location=file:/path/to/directory/``

and launch Tomcat.





