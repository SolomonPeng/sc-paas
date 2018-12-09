FROM @docker.image.prefix@/microservice/java:8
VOLUME /tmp
ENV TZ=Asia/Shanghai
ENV CONFIG_LABEL \
    CONFIG_URI
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ADD @project.build.finalName@.jar app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dconfig.label=${CONFIG_LABEL}","-Dconfig.uri=${CONFIG_URI}","-jar","/app.jar"]