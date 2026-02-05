FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21
ENV TZ=Europe/Oslo
COPY /target/tiltak-notifikasjon-0.0.1-SNAPSHOT.jar /app/app.jar

WORKDIR /app
CMD ["-jar","app.jar"]
