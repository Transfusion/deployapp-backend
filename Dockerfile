FROM ghcr.io/graalvm/graalvm-ce:ol9-java11-22.3.1
#https://stackoverflow.com/questions/22111060/what-is-the-difference-between-expose-and-publish-in-docker
EXPOSE 8080
ARG JAR_FILE=./build/libs/deployapp-backend.jar
WORKDIR /app
COPY ${JAR_FILE} app.jar
RUN ls /app
RUN java --version
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar ${0} ${@}"]