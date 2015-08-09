# MusictextService
A RESTful webservice for the Musictext application. 

This java application provides a web interface to the server-side database and application of Musictext, an application for the recommendation of songs based on the listener preferences and the current context he is in. It can be deployed to TomCat or as an AppEngine component for the Google Cloud Platform.

We provide a basic definition of a database, for mySQL, and a java wrapper to it. We also include a set of test cases for the service. The test cases run using the JMockit library.

The application uses a Jess wrapper, which requires a licensed copy of the Jess Engine. Jess is a rule engine and scripting environment, available at http://herzberg.ca.sandia.gov