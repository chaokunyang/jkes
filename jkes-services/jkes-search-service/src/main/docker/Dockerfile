FROM hub.c.163.com/library/java:8-jre-alpine
VOLUME /tmp
ADD search-service-1.0.0.jar app.jar
RUN sh -c "touch /app.jar"
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar"]