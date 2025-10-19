# Build stage
#
FROM gradle:8.8-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean
RUN gradle bootJar
#
# Package stage
#
FROM openjdk:21
ARG JAR_FILE=build/libs/*.jar
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
EXPOSE ${PORT}
ENTRYPOINT ["java","-jar","/app.jar"]