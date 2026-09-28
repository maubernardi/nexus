# Immagine di runtime del backend NEXUS. Il jar è costruito prima (CI: ./mvnw package), qui si estrae a layer:
# dipendenze e codice applicativo in layer separati, così un deploy scarica di solito solo il codice cambiato.
FROM eclipse-temurin:25.0.4.1_1-jre-alpine AS extract
WORKDIR /build
COPY target/nexus-backend-*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:25.0.4.1_1-jre-alpine
LABEL org.opencontainers.image.source="https://github.com/maubernardi/nexus" \
      org.opencontainers.image.title="nexus-backend" \
      org.opencontainers.image.description="NEXUS — API Spring Boot"
RUN addgroup -S nexus && adduser -S -G nexus -H -s /sbin/nologin nexus
WORKDIR /app
COPY --from=extract /build/extracted/dependencies/ ./
COPY --from=extract /build/extracted/spring-boot-loader/ ./
COPY --from=extract /build/extracted/snapshot-dependencies/ ./
COPY --from=extract /build/extracted/application/ ./
USER nexus
EXPOSE 8080
# Memoria dimensionata sul limite del container; in caso di OutOfMemory il processo termina e Docker lo riavvia.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
