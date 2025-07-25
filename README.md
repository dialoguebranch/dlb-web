# Dialogue Branch Web Services
The Dialogue Branch Web Services is a set of web service tools that allows you to execute Dialogue
Branch scripts in a server environment. 

For additional information please refer to www.dialoguebranch.com and specifically the
documentation available at www.dialoguebranch.com/docs

## First Things First
Whether you want to deploy or develop on this code, first make sure you have pulled both this 
`dlb-web` repository and the `dlb-core-java` repository to your local machine. If your git folder is
 `${GIT}`, your folder structure should look like this:

* `${GIT}`/dialoguebranch/dlb-core-java/
* `${GIT}`/dialoguebranch/dlb-web/

## 1. Deploying a Standalone Dialogue Branch Web Service using Docker
The quickest way to start playing with a Dialogue Branch Web Service is to deploy a simple, stand-
alone instance as a Docker container. To do so, follow these steps:

### 1.1. Prepare configurations
* Create a `gradle.properties` file in the `dlb-web/dlb-web-service/` folder by copying the existing 
`gradle.docker-standalone.properties`. This contains workable default configuration values. 
* Prepare a `users.xml` file in the `dlb-web/dlb-web-service/config/` folder (copy the existing 
`users-example.xml` file).
  * You can define your own users here, or simply keep the example `client::client`, 
  `editor::editor`, and `admin::admin` users.

### 1.2. Build and Run Docker Image
* Open a terminal and enter your `{GIT}/dialoguebranch/` folder (containing `/dlb-web/` and 
`/dlb-core-java/` repositories)
* Enter the following command to build the Docker image: `docker build --no-cache -t
dlb-web-service -f ./dlb-web/dlb-web-service/standalone.Dockerfile .`
* Enter the following command to run the Docker image: `docker run -itd -p 8089:8089 --name dlb-web-service dlb-web-service`
* Open a Web Browser and navigate to `http://localhost:8089/dlb-web-service/` (you should see 
the Swagger documentation page of your running Web Service).

## 2. Deploying DLB Web Service with Keycloak using Docker (experimental)
Instead of using the built-in user management (i.e. with user credentials as defined in the 
users.xml file), you can choose to delegate account management to a Keycloak server. To set this up
on your local machine follow these steps.

### 2.1. Prepare configuration
* Create a `gradle.properties` file in the `dlb-web/dlb-web-service/` folder by copying the existing 
`gradle.docker-with-keycloak.properties`. This contains workable default configuration values.

### 2.2. Build DLB Web Service Image
* Make sure that Docker is running. You can install Docker Desktop on your computer.
* Open a terminal and enter your `{GIT}/dialoguebranch/` folder (containing `/dlb-web/` and 
`/dlb-core-java/` repositories)
* Enter the following command to build the Docker image: `docker build --no-cache -t dlb-web-service -f ./dlb-web/dlb-web-service/with-keycloak.Dockerfile .`

For faster development, you may build the web service and its WAR file separately outside the Docker image and then bind to it from the Docker container.
Then you don't need to rebuild the image at every change in your code.

* Open a terminal and enter the `{GIT}/dialoguebranch/dlb-web/dlb-web-service` folder.
* Build the dev image once with: `docker build --no-cache -t dlb-web-service:dev -f ./dlb-web/dlb-web-service/with-keycloak-dev.Dockerfile .`
* Build the WAR file with this command: `./gradlew clean updateConfig build`. You can repeat this command when you want to deploy your code changes.

### 2.3. Running DLB Web Service and Keycloak using Docker Compose
* In your terminal, navigate to the `{GIT}/dialoguebranch/dlb-web/docker-compose/` folder.
* Run using `docker compose -f compose-with-keycloak.yml up`
* Or, if you built the dev image, run `docker compose -f compose-with-keycloak-dev.yml up -d`, and then you can close this terminal.

### 2.4. Test the Setup
* Open a web-browser in http://localhost:8080/. This should bring up the Keycloak administration
  panel. Your browser will probably show a warning about an insecure connection, because you are
  using a self-signed certificate. Log in with the default username and password (admin/admin), and do the following:
  * Make sure that the currently selected Realm is `dialoguebranch`.
  * Create a user with e.g. username `user`, fill in an email address, first name and last name, check "email verified" and set a fixed password (e.g. `user`).
* Open in another tab: http://localhost:8089/dlb-web-service/. This should open up the Swagger pages
  for Dialogue Branch Web Service. Do the following:
  * Select the `/auth/login/` end-point, and click on "Try it out".
  * In the Request body, set both `user` and `password` fields to "user".
  * Click on "Execute". You should receive a response like this:

```
{
  "user": "user",
  "role": "user",
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIi..."
}
```

## 3. Deploying a standalone DLB External Variable Service using Docker

### 3.1. Prepare configurations
* Create a `gradle.properties` file in the `dlb-web/dlb-external-var-service/` folder by copying the existing 
`gradle.docker-standalone.properties`. This contains workable default configuration values. 
* Prepare a `service-users.xml` file in the `dlb-web/dlb-web-service/config/` folder (copy the existing 
`service-users-example.xml` file).
  * You can define your own users here, or simply keep the example `dlb-web-service::dlb-web-service` user.

### 3.2. Build and Run Docker Image
* Open a terminal and enter your `{GIT}/dialoguebranch/` folder (containing the `/dlb-web/` 
  repository).
* Enter the following command to build the Docker image: `docker build --no-cache -t dlb-external-var-service -f ./dlb-web/dlb-external-var-service/standalone.Dockerfile .`
* Enter the following command to run the Docker image: `docker run -itd -p 8090:8090 --name dlb-external-var-service dlb-external-var-service`
* Open a Web Browser and navigate to `http://localhost:8090/dlb-external-var-service/` (you should see 
  the Swagger documentation page of your running External Variable Service).

## 4. Development Setup
Getting started with development on the Dialogue Branch Web Service tools should be relatively 
straightforward. If you run into issues after following the guide below, please contact 
`info@dialoguebranch.com`

### 4.1. File Structure
Let's assume that `{GIT}` is your local git folder (e.g. `/Users/johnny/git/`). Then, make 
sure you have the following file structure:

* `{GIT}/dialoguebranch/dlb-web` (this repository)
* `{GIT}/dialoguebranch/dlb-core-java` (the core Java Library, which can be found here: 
https://github.com/dialoguebranch/dlb-core-java) - the Dialogue Branch Web Service operates
mostly as a server-wrapper around the core Java Library that is used for parsing and executing
dialogue scripts.

### 4.2. IntelliJ Configuration
On the IntelliJ Welcome Screen, select `Open` and select the `{GIT}/dialoguebranch/dlb-web` 
folder.

This will import the following three modules in your IDEA:
* dlb-core-java
* dlb-external-web-service (this is an optional stand-alone server component that allows you to 
easily integrate Dialogue Branch data (Variables) with other existing server components)
* dlb-web-service

If all goes well, the project should compile without issue. If something is wrong, please verify
the following settings in IntelliJ:

* Under `File` -> `Project Structure` make sure the following settings are correct:
  * `Project Settings` -> `Project`
    * Select an SDK of version 17 or higher.
    * Set the Language Level to 17.
  * `Project Settings` -> `Modules`
    * Set the Module SDK to an SDK with version 17 or higher.
* Under `IntelliJ IDEA` -> `Settings...`
  * Go to `Build, Execution, Deployment` -> `Build Tools` -> `Gradle`
    * Make sure the Gradle JVM is set to a JVM of version 17 or higher.

### 4.3. Configuration files
Before deploying, create the following configuration files:

* dlb-web/dlb-external-var-service/gradle.properties
* dlb-web/dlb-external-var-service/config/users.xml
* dlb-web/dlb-web-service/gradle.properties
* dlb-web/dlb-web-service/config/users.xml
* dlb-web-client/config.json
