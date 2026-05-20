# === Stage 1: Build ===
# Use a JDK base image and set the working directory
FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /workspace

# Download project dependencies first (for caching them in the Docker layer)
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew
RUN ./gradlew --no-daemon dependencies

# Copy source code and build the fat JAR
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# Extract the fat JAR into specialized layers (this improves the caching in Stage 2)
RUN java -Djarmode=tools -jar build/libs/*.jar extract --layers --launcher --destination extracted

# === Stage 2: Runtime ===
# Use a JRE base image and set the working directory
FROM eclipse-temurin:25-jre-noble
WORKDIR /app

# Copy application layers sequentially for granular Docker caching.
# Heavy, unchanged layers (dependencies) are copied first to remain cached,
# while the more frequently changing application layer (own code) is kept at the bottom.
# Assign file ownership to UID:GID 1000 so that the files are owned by the non-root user
COPY --from=build --chown=1000:1000 /workspace/extracted/dependencies/ ./
COPY --from=build --chown=1000:1000 /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=1000:1000 /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=1000:1000 /workspace/extracted/application/ ./

# Switch to the non-privileged user for maximum security compliance
USER 1000:1000

# Document which port needs to be exposed
EXPOSE 8080

# Run the application using the official Spring Boot launcher.
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
