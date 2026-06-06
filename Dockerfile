# use base jdk image
From eclipse-temurin:17-jdk
# set the working dir
WORKDIR /app

ARG VERSION=1.0.0
LABEL version=$VERSION
# take jar file location
ARG JAR_FILE

COPY $JAR_FILE /app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
