FROM maven:3.6.2-jdk-11
WORKDIR /workspace

RUN mkdir json expression runtime providers providers/base providers/remote server server/api server/app

COPY pom.xml ./
COPY json/pom.xml ./json
COPY expression/pom.xml ./expression
COPY server/pom.xml ./server
COPY server/api/pom.xml ./server/api
COPY server/app/pom.xml ./server/app
COPY providers/pom.xml ./providers
COPY providers/base/pom.xml ./providers/base
COPY providers/remote/pom.xml ./providers/remote

RUN mvn package -B -DexcludeGroupIds=io.stephub
COPY . ./
RUN mvn package
