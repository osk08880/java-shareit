# Используем Java 21 JRE
FROM eclipse-temurin:21-jre-jammy

# Создаём рабочую папку
WORKDIR /app

# Копируем jar приложения
COPY target/*.jar app.jar

# Копируем скрипт wait-for-it.sh
COPY wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh

# Указываем команду запуска
# Ждём готовности базы (db:5432), затем запускаем Spring Boot
CMD ["./wait-for-it.sh", "db:5432", "--", "java", "-jar", "app.jar"]
