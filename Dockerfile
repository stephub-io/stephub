FROM maven:3.6.2-jdk-11
WORKDIR /workspace

RUN mkdir json expression runtime providers providers/base providers/remote

COPY pom.xml ./
COPY json/pom.xml ./json
COPY expression/pom.xml ./expression
COPY runtime/pom.xml ./runtime
COPY providers/pom.xml ./providers
COPY providers/base/pom.xml ./providers/base
COPY providers/remote/pom.xml ./providers/remote

RUN mvn package -B -DexcludeGroupIds=io.stephub
COPY . ./
RUN mvn package
