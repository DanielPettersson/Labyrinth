FROM openjdk:13-alpine
MAINTAINER daniel.g.pettersson@gmail.com
COPY target/labyrinth-0.0.1-SNAPSHOT.jar /opt/labyrinth/
ENTRYPOINT ["java"]
CMD ["-jar", "/opt/labyrinth/labyrinth-0.0.1-SNAPSHOT.jar"]
EXPOSE 8080

