FROM gcr.io/distroless/java21-debian12:nonroot
ENV TZ=Europe/Oslo
COPY /target/tiltak-notifikasjon-0.0.1-SNAPSHOT.jar app.jar

CMD ["-jar", "app.jar"]
