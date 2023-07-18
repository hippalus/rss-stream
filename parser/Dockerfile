FROM maven:3.8.5-openjdk-17 as module-build

WORKDIR /module/app

COPY . .

RUN mvn clean install -DskipTests=true


FROM openjdk:17.0.2-jdk-slim as production

USER root

RUN apt-get update -y && apt-get install -y jq curl

USER 1000

WORKDIR /app

COPY --from=module-build --chown=1000:1000 /module/app/target/parser-0.0.1-SNAPSHOT.jar ./parser-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "parser-0.0.1-SNAPSHOT.jar"]

FROM production as development

USER root

COPY --from=module-build /usr/share/maven /usr/share/maven
COPY --from=module-build --chown=1000:1000 /module/app /development
COPY --from=module-build --chown=1000:1000 /root/.m2 /opt/jboss/.m2

RUN ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

USER 1000