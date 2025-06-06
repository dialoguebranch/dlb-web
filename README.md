# Dialogue Branch Web Services
The Dialogue Branch Web Services is a set of web service tools that allows you to execute Dialogue
Branch scripts in a server environment. 

For additional information please refer to www.dialoguebranch.com and specifically the
documentation available at www.dialoguebranch.com/docs

## Deploying a Dialogue Branch Web Service using Docker
The quickest way to start playing with a Dialogue Branch Web Service is to deploy an instance 
as a Docker container. To do this, follow these steps:

* Create a `gradle.properties` file in the `dlb-web/dlb-web-service/` folder (copy the existing 
`gradle.sample.properties` file)
* Prepare a `users.xml` file in the `dlb-web/dlb-web-service/config/` folder (copy the existing 
`users-example.xml` file).

* Open a terminal and enter your `{GIT}/dialoguebranch/` folder (containing `/dlb-web/` and 
`/dlb-core-java/` repositories)
* Enter the following command to build the Docker image: `docker build --no-cache -t dlb-web-service:1.2.4 -f dlb-web/Dockerfile .`
* Enter the following command to run the Docker image: `docker run -itd -p 8089:8089 --name DLB_Web_Service dlb-web-service:1.2.4`
* Open a Web Browser and navigate to `http://localhost:8089/dlb-web-service/` (you should see 
the Swagger documentation page of your running Web Service).

## Development Setup
Getting started with development on the Dialogue Branch Web Service tools should be relatively 
straightforward. If you run into issues after following the guide below, please contact 
`info@dialoguebranch.com`

### File Structure
Let's assume that `{GIT}` is your local git folder (e.g. `/Users/johnny/git/`). Then, make sure you
have the following file structure:

* `{GIT}/dialoguebranch/dlb-web` (this repository)
* `{GIT}/dialoguebranch/dlb-core-java` (the core Java Library, which can be found here: 
https://github.com/dialoguebranch/dlb-core-java) - the Dialogue Branch Web Service operates mostly
as a server-wrapper around the core Java Library that is used for parsing and executing dialogue 
scripts.

### IntelliJ Configuration
On the IntelliJ Welcome Screen, select `Open` and select the `{GIT}/dialoguebranch/dlb-web` folder.

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

### Configuration files
Before deploying, create the following configuration files:

* dlb-web/dlb-external-var-service/gradle.properties
* dlb-web/dlb-external-var-service/config/users.xml
* dlb-web/dlb-web-service/gradle.properties
* dlb-web/dlb-web-service/config/users.xml
* dlb-web-client/config.json
