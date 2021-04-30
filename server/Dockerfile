ARG FRONTEND_BUILD_IMAGE
ARG BACKEND_BUILD_IMAGE
FROM $FRONTEND_BUILD_IMAGE as frontend-build

FROM $BACKEND_BUILD_IMAGE as backend-build
WORKDIR /workspace/server/app
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM openjdk:11-jre-slim-buster
VOLUME /tmp
ARG DEPENDENCY=/workspace/server/app/target/dependency
COPY --from=backend-build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=backend-build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=backend-build ${DEPENDENCY}/BOOT-INF/classes /app
COPY --from=frontend-build /workspace/dist/app /app/META-INF/resources/app
ARG BUILD_VERSION
RUN sed -i "/build.version=/ s/=.*/=${BUILD_VERSION:-v1.0.0-SNAPSHOT}/" /app/META-INF/build-info.properties

ENTRYPOINT ["java", "-cp", "app:app/lib/*", "io.stephub.server.ServerApplication"]