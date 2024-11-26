FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0
WORKDIR /build

ENV PORT=7024

ENV DB_HOST="localhost"
ENV DB_PORT="5432"
ENV DB_NAME="postgres"
ENV DB_SCHEMA="public"
ENV DB_USERNAME="postgres"

COPY . .

RUN sbt clean compile stage

EXPOSE ${PORT}

CMD [ "sh", "-c", "/build/target/universal/stage/bin/chatmanagement -Dplay.http.secret.key='CTf:G3gf:90Latabg@5241AB`R5W:1uDFN' -Dhttp.port=${PORT}" ]
