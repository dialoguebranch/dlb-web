### From your GIT/dialoguebranch/ folder
### docker build --no-cache -t dlb-external-var-service -f ./dlb-web/dlb-external-var-service/standalone.Dockerfile .
### docker run -itd -p 8090:8090 --name dlb-external-var-service dlb-external-var-service
### (User 'docker system prune -a') to clean up the cache and free up disk space

# Use an official Java runtime as a parent image
FROM tomcat:11.0

# Edit the server.xml, changing the port from 8080 to 8090
RUN sed -i 's/port="8080"/port="8090"/' ${CATALINA_HOME}/conf/server.xml

# Make port 8090 available to the world outside this container
EXPOSE 8090

### Prepare the source files and data folders

# Copy the local source folder into the container
COPY ./dlb-web/dlb-external-var-service/ /usr/local/dialogue-branch/source/dlb-web/dlb-external-var-service/
RUN for f in `find /usr/local/dialogue-branch/source -name "gradlew" -print`; do chmod 755 $f && sed -i -e 's/\r//' $f; done

# Create some data folders used by the services
RUN mkdir /usr/local/dialogue-branch/data/
RUN mkdir /usr/local/dialogue-branch/data/dlb-external-var-service/

### Next, build and deploy the External Variable Service

# Set the working directory to the DLB External Variable Service source folder
WORKDIR /usr/local/dialogue-branch/source/dlb-web/dlb-external-var-service/

# Execute a clean build
RUN ./gradlew clean updateVersion build

# Copy the generated .war file into the tomcat webapps
RUN cp /usr/local/dialogue-branch/source/dlb-web/dlb-external-var-service/build/libs/dlb-external-var-service-1.2.5.war /usr/local/tomcat/webapps/dlb-external-var-service.war

# Copy the service-users.xml file into the data folder
RUN cp /usr/local/dialogue-branch/source/dlb-web/dlb-external-var-service/config/service-users.xml /usr/local/dialogue-branch/data/dlb-external-var-service/

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


