Gpio debugger UI
================

Introduction
------------
This is the client part of a debugging tool based on *[bulldog library](https://github.com/SilverThings/bulldog "Bulldog")*. 
To use it, simply install it via Maven.

Functionality
--------------
The debugging tool is now capable of:
* interfacing with GPIO pins (i.e. reading/writing signals on the pin)
* sending I2C requests
* sending SPI requests
* registering interrupt listeners (not working properly yet, experimental)
