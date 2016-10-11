# Proxy Server
This is a Java Implementation of a Man in the Middle Proxy Server. It has the following features:
* Supports HTTP Requests
* Has an Admin Console
* Can Block requests to any Domain
* Supports Caching of pages

## Requirements
* JDK

## Installation
Setting up the proxy is a straightforward process, just open a terminal in the directory and run:
```
javac VisualConsole.java
```
This will compile all the java files in the directory.

## Usage
To run the proxy server and show the admin console, run:
```
java VisualConsole
```

To start the server, when the admin console appears run `start $portnumber`, where portnumber is the desired port to run the proxy on. For a full list of available commands and the syntax, please refer to the `Proxy Report.pdf` file in the repo.
