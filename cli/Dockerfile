ARG BUILD_IMAGE
FROM $BUILD_IMAGE as build
WORKDIR /workspace/cli
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM openjdk:11-jre-slim-buster
VOLUME /tmp
ARG DEPENDENCY=/workspace/cli/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ARG BUILD_VERSION
RUN sed -i "/build.version=/ s/=.*/=${BUILD_VERSION:-v1.0.0-SNAPSHOT}/" /app/META-INF/build-info.properties

ENTRYPOINT ["java", "-cp", "app:app/lib/*", "io.stephub.cli.Application"]