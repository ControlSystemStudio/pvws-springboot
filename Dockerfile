# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk AS builder
WORKDIR /build
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
COPY . .
RUN mvn --batch-mode --update-snapshots clean package -DskipTests

FROM eclipse-temurin:17-jre AS runner
WORKDIR /app
COPY --from=builder /build/target/pvws*.jar ./pvws.jar
CMD ["java", "-jar", "/app/pvws.jar", "--spring.config.name=application"]
