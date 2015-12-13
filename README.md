# EggBot
All you need to make your own EggBot.

This repository contains the Arduino code and a computer application to print files.

## Licence

 * Hardware is under Creative Commons Attribution-ShareAlike 3.0
 * Arduino code is under GNU GPL
 * The GcodeSender (computer application) application is under MIT Licence
 
## Electronics 
 * 1x Arduino uno
 * 1x CNC shield for Arduino UNO
 * 2x A4988 Stepper driver
 * 2x Nema 17 Stepper motor
 * 1x mini servo
 * 1x 12 volt power supply (min 1A)

## Computer application

![alt text](https://raw.githubusercontent.com/fablab-fribourg/EggBot/master/docs/images/screenshot1.png "GcodeSender")

**Steps to print a file**

* start the application
* choose and open the port where the arduino is connected (donc forget to upload the Arduino sketch first)
* choose the file you want to print 
  * this is a simple GCode file
  * you can convert an image with the last version of inkscape ([demo](https://www.youtube.com/watch?v=4jYKMAjzK3A))
* *[Optional]* adjust the limits
* *[Optional]* adjust the scale
* *[Optional]* define home
* "Print that egg"
