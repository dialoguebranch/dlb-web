# Dialogue Branch Web Services
The Dialogue Branch Web Services is a set of web service tools that allows you to execute Dialogue
Branch scripts in a server environment. 

For additional information please refer to www.dialoguebranch.com

## Development Setup
Getting started with development on the Dialogue Branch Web Service tools should be relatively 
straightforward. If you run into issues after following the guide below, please contact 
`support@dialoguebranch.com`

### File Structure
Let's assume that `{GIT}` is your local git folder (e.g. `/Users/johnny/git/`). Then, make sure you
have the following file structure:

* `{GIT}/dialoguebranch/dlb-web` (this repository)
* `{GIT}/dialoguebranch/dlb-core-java` (the core Java Library, which can be found here: 
https://github.com/dialoguebranch/dlb-core-java)

### IntelliJ Configuration
On the IntelliJ Welcome Screen, select `Open` and select the `{GIT}/dialoguebranch/dlb-web` folder.

This will import the following three modules in your IDEA:
* dlb-core-java
* dlb-external-web-service
* dlb-web-service

If all goes well, the project's should compile without issue. If something is wrong, please verify
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
