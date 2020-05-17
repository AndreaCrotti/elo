FROM circleci/clojure:lein-2.9.1 AS builder
USER root
COPY . /app

WORKDIR /app
RUN lein uberjar


FROM openjdk:14-alpine

RUN mkdir -p /app /app/resources

COPY --from=builder /app/target/*.jar /app/
COPY --from=builder /app/resources /app/resources

WORKDIR /app

CMD java -jar byf.jar
EXPOSE 3335
