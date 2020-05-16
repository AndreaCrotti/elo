FROM circleci/clojure:lein-2.9.1 AS builder
USER root
COPY . /app

WORKDIR /app
RUN lein uberjar


FROM openjdk:12-alpine

RUN mkdir -p /app /app/resources

COPY --from=builder /app/target/*.jar /app/
COPY --from=builder /app/resources/public /app/resources/public

WORKDIR /app

CMD java -jar byf-0.1.0-SNAPSHOT.jar
EXPOSE 3000
