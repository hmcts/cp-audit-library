# Audit requirements
* Allow exclude some urls from auditing i.e. actuator endpoints from particular addresses ?
* ( We should exclude with caution )
* ( Are we auditing for internal visibility or for security penetration type reasons )
* For every incoming request we should send an audit message for request and one for response
* Presume we should log everything even 404 requests ?

Request 
* Url
* Request Headers
* Request body

Response
* Url
* Response Headers
* Response body
* Response status i.e. 200 / 400 / 500 etc


# Possible gaps ... can only see the bodies ?
* Where see url ?
* Where see headers ?
* Where see status ?


# Example Payloads
Taken from running cp-access-facade and posted some sample requests to access-facade-demo-service
Captured the audit payloads into test/resources/auditPayloads
curl -X POST \
-H "content-type: application/json" \
-H "cgheader: cgvalue" \
--data '{"cgfield" : "cgvalue"}' \
http://localhost:8080/api/echo
( 
and the same without the content-type produces a 415
{"timestamp":"2025-10-23T10:37:24.900Z","status":415,"error":"Unsupported Media Type","path":"/api/echo"}%
)


# Queries / Issues
Why add the complexity of OpenApi ? Surely we just want to log the url headers and bodies?
The AuditPayloadGenerationService is not really a service just a simple mapper
Why use a bean that needs setting in calling app, lets use the same bean with parameters


# Testing
We want to confirm that when we hit any endpoint, we get the audit message sent to artemis jms
We create a dummy spring boot application which inherits the default AuditFilter
We create a dummy root endpoint which we can send a message to
We spin up activemq-artemis in docker container with anonymous enabled
We create a JmsMessageListener that listen to the same topic and pass this to our mock service

Thus we can verify the content of messages that have been sent via activemq

Note that we could view the messages in artemis console on http://0.0.0.0:8161/console/artemis
... although if we consume them in our TestJmsListener the queues will be empty

