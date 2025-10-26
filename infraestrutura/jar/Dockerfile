FROM openjdk:21-jdk

RUN microdnf install -y curl

WORKDIR /app

ARG JAR_URL="https://github.com/Grupo-4-PI/back-end/releases/download/v1.1.2/extrator-tratamento-dados-xls-xlsx-1.0-SNAPSHOT.jar"

RUN curl -L -o app.jar ${JAR_URL}

EXPOSE 8080

CMD [ "java", "-jar", "app.jar" ]