FROM eclipse-temurin:21-jdk

RUN apt-get update && apt-get install -y curl

WORKDIR /app

ARG JAR_URL="https://github.com/Grupo-4-PI/back-end/releases/download/v1.2.0/extrator-tratamento-dados-xls-xlsx-1.0-SNAPSHOT.jar"

RUN curl -L -o app.jar ${JAR_URL}

EXPOSE 8080

CMD [ "java", "-Xmx3g", "-jar", "app.jar" ]