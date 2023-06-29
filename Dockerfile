#Start with a base image containing Java runtime
FROM openjdk:17-jdk-slim as build

#Information around who maintains the image
MAINTAINER ladunski.com

# Add the application's jar to the container
COPY target/shoppingCartDemo-1.0-SNAPSHOT.jar shoppingCartDemo-1.0-SNAPSHOT.jar

#execute the application
ENTRYPOINT ["java","-jar","/shoppingCartDemo-1.0-SNAPSHOT.jar"]