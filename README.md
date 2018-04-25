Food4me webservice
==================

The food4me webservice is a webservice that can provide advices on food intake,
based on measurements regarding food intake, biomarkers, physical parameters
and certain SNPs. The advices and decision trees themselves are not provided, 
as they are not available for free. However, when using this webservice, you 
can load your own data, or use the example data for testing.   

Installation
------------
For development, a copy of [grails][1] is needed in order to run the application. 
Currently, version 2.5.6 of grails is used.

For production, you can use a generated WAR file. In order to run the WAR, an 
application server is required. The only supported one is [Tomcat][2], either 
from the 6.x or 7.x line, though it will most likely work on others.

In addition, a PostgreSQL database is required. When starting the application
in development mode, the tables will be automatically generated, if they don't
exist yet. 

Configuration
-------------
Configuration can be done using an externalized configuration file in 
`~/.grails/food4me-config.groovy`. Using this file, you can overwrite the default
configuration settings as well as the database connection settings. An example
file can be found in `example-settings.groovy`

  [1]: http://grails.org/
  [2]: http://tomcat.apache.org/

## Development

Test the app with

```
./grailsw test-app
```
