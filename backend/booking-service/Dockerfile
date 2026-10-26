# Build context: the backend/ directory (so the parent pom.xml is available).
#   docker build -f booking-service/Dockerfile -t sports-arena-booking .

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
# Maven needs every module's pom.xml listed in the parent, even when building one module.
COPY auth-service/pom.xml auth-service/
COPY user-service/pom.xml user-service/
COPY catalog-service/pom.xml catalog-service/
COPY booking-service/pom.xml booking-service/
COPY gateway-service/pom.xml gateway-service/
RUN mvn -B -q -pl booking-service -am dependency:go-offline
COPY booking-service/src booking-service/src
RUN mvn -B -q -pl booking-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 app
COPY --from=build /build/booking-service/target/booking-service-0.0.1-SNAPSHOT.jar app.jar
USER app
# Fits a 512 MB container: cap heap, small thread stacks, serial GC, faster startup.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1 -XX:+ExitOnOutOfMemoryError"
# The port comes from $PORT when set (Render), otherwise the service default.
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
