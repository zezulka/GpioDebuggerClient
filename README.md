[![Build Status](https://travis-ci.org/zezulka/GpioDebuggerClient.svg?branch=master)](https://travis-ci.org/zezulka/GpioDebuggerClient)

Gpio debugger client
================

Introduction
------------
This is the client part of a debugging tool based on *[bulldog library](https://github.com/SilverThings/bulldog "Bulldog")*. Client works only with Java 8. 

Functionality
--------------
The debugging tool is now capable of:
* interfacing with GPIO pins (i.e. reading/writing signals on the pin)
* sending I2C requests
* sending SPI requests
* registering interrupt listeners

Installation
------------
You can either download the latest release from *[the releases on this repository](https://github.com/zezulka/GpioDebuggerClient/releases)* 
or launch `mvn clean install` in the root directory of this project. The path to the
assembled JAR file is target/GpioDebuggerClient-${version}-jar-with-dependencies.jar
Issue the command `java -jar target/GpioDebuggerClient-${version}-jar-with-dependencies.jar` 
(or if you downloaded the client from the releases, modify the path accordingly)
to launch the client.

Running with OpenJDK on Linux
------------
Please make sure you have the `openjfx` package installed.
Alternatively, you can manually install OpenJFX into the Java home.
For more information, please visit https://chriswhocodes.com/ .
