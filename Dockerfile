FROM maven:3.6.2-jdk-11
WORKDIR /workspace

RUN mkdir json expression runtime provider providers providers/base providers/util providers/remote providers/wiremock

COPY pom.xml ./
COPY json/pom.xml ./json
COPY expression/pom.xml ./expression
COPY provider/pom.xml ./provider
COPY runtime/pom.xml ./runtime
COPY providers/pom.xml ./providers
COPY providers/util/pom.xml ./providers/util
COPY providers/base/pom.xml ./providers/base
COPY providers/remote/pom.xml ./providers/remote
COPY providers/wiremock/pom.xml ./providers/wiremock

RUN mvn package -B -DexcludeGroupIds=io.stephub
COPY . ./
RUN mvn package