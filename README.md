# Sensor network with Android

![architecture](./doc/architecture.jpg)

## Background and motivation

Once I used RasPi/Arduino for IoT prototyping, ending up with unsatisfying results: complicated physical wiring problems.

I stopped using RasPi/Arduino for IoT prototyping, and I started thinking of a combination of [PIC16F1-based sensor network](https://github.com/araobp/sensor-network) and Android to simplify physical wiring.

## Android apps

### Driver Service

<<<<<<< HEAD
### Edge computing Service
=======
CLI is the entry of sensor data from sensor network. CLI is also a main user interface of this Android application.

![screenshot_cli](./doc/screenshot_cli.png)
>>>>>>> origin/master

### CLI Activity

<<<<<<< HEAD
![screenshot_cli](./doc/screenshot_cli.png)

### Visualizer Activity
=======
Edge computing has multiple worker threads in a thread pool to work on CPU-intensive computing with multiple CPU cores.

### Visualizer

Visualizer visualizes the output from edge computing.
>>>>>>> origin/master

## Use case: bicycle

I develop in-bicycle network using the output from [sensor-network](https://github.com/araobp/sensor-network). It is like a cheap version of [CANopen](https://www.can-cia.org/canopen/) :-)

### Technical requirements

- Very low power consumption
- Cheap
- Show current speed, acceleration, temperature and humidity on a character LCD
- Save time-series data (speed, temperature, humidity, acceleration and location) onto Android smartphone

### Thing: my bicycle

![network](./doc/network.jpg)

## Development tools

### IDE
- Android: [Android Studio](https://developer.android.com/studio/index.html)

### FTDI driver
- [Android Java D2XX driver](http://www.ftdichip.com/Drivers/D2XX.htm)

## Links
- [My smart phone: ASUS ZenFone Lazer](https://www.asus.com/Phone/ZenFone-2-Laser-ZE500KL/)
- [Dragon board (Quallcomm)](https://developer.qualcomm.com/hardware/dragonboard-410c)
- [CANopen](https://www.can-cia.org/canopen/)
