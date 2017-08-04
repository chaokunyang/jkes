FROM hub.c.163.com/library/java:8-jre-alpine
VOLUME /tmp
ADD jkes-search-service-1.0.0.jar app.jar
RUN sh -c "touch /app.jar"
ENV JAVA_OPTS=""
ENV APP_ARGS="--spring.profiles.active=test"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar $APP_ARGS"]