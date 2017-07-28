# Sensor network with Android

![flow-based-programming](./doc/flow_based_programming.jpg)

## Background and motivation

### Edge computing

I have developed Plug&Play protocol for [PIC16F1-based sensor network](https://github.com/araobp/sensor-network). Next, I will develop a framework for Android to add edge computing capabilities to my sensor network.

### Why Android

Android framework is based on [https://en.wikipedia.org/wiki/Actor_model](https://en.wikipedia.org/wiki/Actor_model). Actor Model is the basis of concurrent computing in the area of telecommunication systems. Once, I developed [a messaging capability for network controller (i.e., SDN controller)](https://github.com/o3project/odenos/tree/develop/src/main/java/org/o3project/odenos/remoteobject/messagingclient) based on Actor Model to avoid thread collision on a concurrent computing system. In the past (25 years ago), I also developed [a messaging capability for central office switch](https://github.com/araobp/neutron-lan/blob/master/doc/sdn_in_the_past.md).


## Android apps

### CLI

![screenshot_cli](./doc/screenshot_cli.png)

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
- [EventBus](https://github.com/greenrobot/EventBus#add-eventbus-to-your-project)
- [CANopen](https://www.can-cia.org/canopen/)
