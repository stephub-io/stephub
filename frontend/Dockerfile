FROM node:12.7-alpine as ui-build

WORKDIR /workspace

COPY ./package.json ./package-lock.json ./angular.json ./tsconfig.json ./tslint.json ./

RUN npm install

COPY ./projects ./projects

RUN npm run ci
