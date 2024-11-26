FROM amazoncorretto:21

RUN mkdir -p /home/app

COPY ./target/universal/stage/ /home/app/

ENV PORT=7021

ENV DB_HOST="localhost"
ENV DB_PORT="5432"
ENV DB_NAME="postgres"
ENV DB_SCHEMA="public"
ENV DB_USERNAME="postgres"

EXPOSE ${PORT}

CMD [ "sh", "-c", "/home/app/bin/usermanagement -Dplay.http.secret.key='CTf:G3gf:90Latabg@5241AB`R5W:1uDFN' -Dhttp.port=${PORT}" ]
