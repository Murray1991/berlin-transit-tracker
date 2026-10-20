
# Build application
FROM amazoncorretto:26 AS BUILDER
WORKDIR /app

# Provides xargs
RUN dnf install -y findutils

COPY gradlew build.gradle settings.gradle ./
COPY gradle gradle
COPY src src

# Build (skip tests for faster builds)
RUN chmod +x gradlew
RUN ./gradlew clean build -x test

# Run application
FROM amazoncorretto:26
WORKDIR /app

COPY --from=BUILDER /app/build/libs/*-SNAPSHOT.jar api-gateway.jar
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]
