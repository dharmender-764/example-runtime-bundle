# requires Docker version 17.05.0-ce-rc1, build 2878a85
FROM maven:3.5-jdk-8 as BUILDRB

#COPY src /usr/src/myapp/src
#COPY pom.xml /usr/src/myapp
#COPY .git .git
#RUN mvn -f /usr/src/myapp/pom.xml clean package -DskipTests

FROM openjdk:alpine

RUN apk --update add fontconfig ttf-dejavu

#COPY --from=BUILDRB /usr/src/myapp/target/*.jar /maven/

# Added to copy jar from local target folder
COPY target/*.jar /maven/
COPY jetty-alpn-agent.jar /root/
COPY rmaas-dit-1-2cb23d40da29.json /root/

# set debug=true to get spring boot debug-level logging
ENV debug=false
# set REMOTE_DEBUG=true to enable connections to remote debug port
ENV REMOTE_DEBUG=false

CMD if [ "x$REMOTE_DEBUG" = "xfalse" ] ; then java $JAVA_OPTS -javaagent:/root/jetty-alpn-agent.jar -jar maven/*.jar ; else java $JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n -jar maven/*.jar ; fi
