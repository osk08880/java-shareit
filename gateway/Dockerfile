FROM eclipse-temurin:21-jre-jammy

VOLUME /tmp

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Используем exec форму ENTRYPOINT, чтобы переменные окружения подхватывались Spring Boot
ENTRYPOINT ["java","-jar","/app.jar"]
