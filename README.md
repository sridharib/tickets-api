## Tickets API

This is a sample project to demonstrate ticket service.

###### Commands to build & run this application
```
cd artist
./mvnw clean install
java -jar target/artist-0.0.1-SNAPSHOT.jar
```

Once the server is up & running then verify the service
> curl http://localhost:8080/api/artist/21 - OK  
> curl http://localhost:8080/api/artist/100 - 404

###### Technologies used
```
Java 17
Spring Boot, Webflux(Reactive)
Lombok - to remove boilerplate code
jUnit & Mockito - testing
Maven - build
```

###### Cases not covered
Handling large response from the rest API  
Security, Caching, Containerization, Scalability etc.
