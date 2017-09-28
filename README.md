[![Build Status](https://travis-ci.org/zezulka/GpioDebuggerClient.svg?branch=master)](https://travis-ci.org/zezulka/GpioDebuggerClient)

Gpio debugger client
================

Introduction
------------
This is the client part of a debugging tool based on *[bulldog library](https://github.com/SilverThings/bulldog "Bulldog")*. 

Functionality
--------------
The debugging tool is now capable of:
* interfacing with GPIO pins (i.e. reading/writing signals on the pin)
* sending I2C requests
* sending SPI requests
* registering interrupt listeners (not working properly yet, experimental)
