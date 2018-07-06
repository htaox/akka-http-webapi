An example of Akka HTTP REST(ful) web APIs
====
Developing Modern Applications with Scala: Web APIs with Akka HTTP


Invoking the API using HTTP protocol
====
 - creating new user 
 
	curl -X POST http://localhost:58080/users \
	  -H "Content-Type: application/json" \
	  -d "{\"email\": \"a@b.com\"}"

 - getting all users	  

	curl http://localhost:58080/users

 - getting the user with identifier `1`

	curl http://localhost:58080/users/1

 - modifying the user with identifier `1`
  
	curl -X PUT http://localhost:58080/users/1   \
	  -u a@b.com:password  -H "Content-Type: application/json" \
	  -d "{\"firstName\": \"John\", \"lastName\":\"Smith\"}"
  

Import the certificate into JKS: Create a self signed root certificate
====

	keytool -genkeypair -v \
	  -alias localhost \
	  -dname "CN=localhost" \
	  -keystore src/main/resources/akka-http-webapi.jks \
	  -keypass changeme \
	  -storepass changeme \
	  -keyalg RSA \
	  -keysize 4096 \
	  -ext KeyUsage:critical="keyCertSign" \
	  -ext BasicConstraints:critical="ca:true" \
	  -validity 365
	  
Export the public certificate as certificate.crt
====

	keytool -export -v \
	  -alias localhost \
	  -file certificate.crt \
	  -keypass changeme \
	  -storepass changeme \
	  -keystore akka-http-webapi.jks \
	  -rfc

Invoking the API using HTTPS protocol
====
  
	curl --cacert certificate.crt https://localhost:58083/users# akka-http-webapi
