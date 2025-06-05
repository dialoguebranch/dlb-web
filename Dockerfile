### From your GIT\dialoguebranch folder (containing dlb-web and dlb-core-java repositories)
### docker build --no-cache -t dlb-web-service:1.2.5 -f dlb-web/Dockerfile .
### docker run -itd -p 8089:8089 --name DLB_Web_Service dlb-web-service:1.2.5
### (User 'docker system prune -a') to clean up the cache and free up disk space

# Use an official Java runtime as a parent image
FROM tomcat:11.0

# Edit the server.xml, changing the port from 8080 to 8089
RUN sed -i 's/port="8080"/port="8089"/' ${CATALINA_HOME}/conf/server.xml

# Make port 8089 available to the world outside this container
EXPOSE 8089

### Prepare the source files and data folders

# Copy the local source folder into the container
COPY ./dlb-web/ /usr/local/dialogue-branch/source/dlb-web/
COPY ./dlb-core-java/ /usr/local/dialogue-branch/source/dlb-core-java/
RUN for f in `find /usr/local/dialogue-branch/source -name "gradlew" -print`; do chmod 755 $f && sed -i -e 's/\r//' $f; done

# Create some data folders used by the services
RUN mkdir /usr/local/dialogue-branch/data/
RUN mkdir /usr/local/dialogue-branch/data/dlb-web-service/
RUN mkdir /usr/local/dialogue-branch/data/dlb-external-var-service/

### 
# Take the keycloak SSL certificate and add it to the trusted keystore of the JVM running in this container
# This will allow this DLB Web Service to communicate to a Keycloak instance over HTTPS
COPY ./dlb-web/docker-compose/certs/keycloakcert.pem /usr/local/share/ca-certificates/keycloakcert.crt
RUN keytool -noprompt -import -alias keycloak -file /usr/local/share/ca-certificates/keycloakcert.crt -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -trustcacerts

### Next, build and deploy the External Variable Service

# Set the working directory to the DLB External Variable Service source folder
WORKDIR /usr/local/dialogue-branch/source/dlb-web/dlb-external-var-service/

# Execute a clean build (when building from Windows, make sure the line endings of the "gradlew" script are set to "LF" and not "CRLF")
RUN ./gradlew clean build

# Copy the generated .war file into the tomcat webapps
RUN cp /usr/local/dialogue-branch/source/dlb-web/dlb-external-var-service/build/libs/dlb-external-var-service-1.2.4.war /usr/local/tomcat/webapps/dlb-external-var-service.war

RUN cp /usr/local/dialogue-branch/source/dlb-web/dlb-external-var-service/config/users.xml /usr/local/dialogue-branch/data/dlb-external-var-service/

### Next, build and deploy the Web Service

# Set the working directory to the DLB Web Service source folder
WORKDIR /usr/local/dialogue-branch/source/dlb-web/dlb-web-service/

# Execute a clean build
RUN ./gradlew clean build

# Copy the generated .war file into the tomcat webapps
RUN cp /usr/local/dialogue-branch/source/dlb-web/dlb-web-service/build/libs/dlb-web-service-1.2.4.war /usr/local/tomcat/webapps/dlb-web-service.war

RUN cp /usr/local/dialogue-branch/source/dlb-web/dlb-web-service/config/users.xml /usr/local/dialogue-branch/data/dlb-web-service/

# Set the working directory in the container
WORKDIR /usr/local/tomcat

# Run Tomcat server
CMD ["catalina.sh", "run"]
