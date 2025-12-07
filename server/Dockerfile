FROM eclipse-temurin:21-jre-jammy
VOLUME /tmp

# Копируем jar
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Копируем wait-for-it.sh
COPY wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh

# Ждем базу, затем запускаем сервер
ENTRYPOINT ["sh", "-c", "./wait-for-it.sh db:5432 -- java ${JAVA_OPTS} -jar /app.jar"]
