# --- Build stage ---
# Just a JDK, not a Maven image — the project already carries its own
# Maven wrapper (mvnw), so we use that instead of assuming Maven is
# installed anywhere, inside the container or on whoever's machine
# builds this. That's the whole point of a wrapper.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy the wrapper and pom.xml first, download dependencies before
# copying source — Docker caches this layer, so editing source code
# later doesn't force a full re-download of every dependency.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests -B

# --- Runtime stage ---
# Debian-based JRE (not -alpine) specifically so bash's /dev/tcp trick
# is available for the healthcheck below without installing anything
# extra just for that purpose.
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=5s --timeout=3s --start-period=20s --retries=5 \
  CMD bash -c 'exec 3<>/dev/tcp/localhost/8081' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
