FROM eclipse-temurin:17-jre-jammy

ARG APP_VERSION=1.2.0
LABEL org.opencontainers.image.title="kafka-console-ui" \
      org.opencontainers.image.version="${APP_VERSION}"

WORKDIR /app
RUN mkdir -p /app/config /app/data /app/logs \
    && chown -R 10001:10001 /app

COPY --chown=10001:10001 target/kafka-console-ui.jar /app/kafka-console-ui.jar

ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx512m -Dfile.encoding=UTF-8"
USER 10001:10001
EXPOSE 7766

ENTRYPOINT ["java", "--add-opens=java.base/java.io=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED", "--add-opens=java.base/java.net=ALL-UNNAMED", "-jar", "/app/kafka-console-ui.jar"]
CMD ["--spring.config.additional-location=optional:file:/app/config/", "--logging.home=/app", "--data.dir=/app"]
