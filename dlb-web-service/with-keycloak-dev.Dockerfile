### You can use this Dockerfile for faster development. Compiling Java code and building a WAR file is done outside
### the Docker image. You build the WAR file first. Then you build the Docker image once and start everything up with
### the corresponding Docker Compose file. After that you can compile and build the WAR file again without the
### need to change anything in Docker. Docker Compose just mounts your local WAR file in the Docker container.

### From GIT/dialoguebranch/dlb-web/dlb-web-service
### ./gradlew clean updateVersion build

### From your GIT\dialoguebranch folder (containing dlb-web and dlb-core-java repositories)
### docker build --no-cache -t dlb-web-service:dev -f ./dlb-web/dlb-web-service/with-keycloak-dev.Dockerfile .
### docker run -itd -p 8089:8089 --name DLB_Web_Service dlb-web-service:dev
### (User 'docker system prune -a') to clean up the cache and free up disk space

# Use an official Java runtime as a parent image
FROM tomcat:11.0

# Edit the server.xml, changing the port from 8080 to 8089
RUN sed -i 's/port="8080"/port="8089"/' ${CATALINA_HOME}/conf/server.xml

# Make port 8089 available to the world outside this container
EXPOSE 8089

### Prepare the data folders

# Create some data folders used by the services
RUN mkdir -p /usr/local/dialogue-branch/data/
RUN mkdir /usr/local/dialogue-branch/data/dlb-web-service/

# Copy the users.xml file into the data folder (not for the keycloak version!)
# RUN cp /usr/local/dialogue-branch/source/dlb-web/dlb-web-service/config/users.xml /usr/local/dialogue-branch/data/dlb-web-service/

# Set the working directory in the container
WORKDIR /usr/local/tomcat

# Run Tomcat server
CMD ["catalina.sh", "run"]

### Install the SSL Certificate used by the Keycloack service

# - Open a terminal and enter the {GIT}/dialoguebranch/dlb-web/docker-compose/certs/ folder.
# - Run the following command to generate a certificate and key file for your localhost (see https://letsencrypt.org/docs/certificates-for-localhost/):
#
# openssl req -x509 -out keycloakcert.pem -keyout keycloakkey.pem \
#  -newkey rsa:2048 -nodes -sha256 \
#  -subj '/CN=keycloak' -extensions EXT -config <( \
#   printf "[dn]\nCN=keycloak\n[req]\ndistinguished_name = dn\n[EXT]\nsubjectAltName=DNS:keycloak\nkeyUsage=digitalSignature\nextendedKeyUsage=serverAuth")

# Take the keycloak SSL certificate and add it to the trusted keystore of the JVM running in this container
# This will allow this DLB Web Service to communicate to a Keycloak instance over HTTPS
COPY ./dlb-web/docker-compose/certs/keycloakcert.pem /usr/local/share/ca-certificates/keycloakcert.crt
RUN keytool -noprompt -import -alias keycloak -file /usr/local/share/ca-certificates/keycloakcert.crt -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -trustcacerts

