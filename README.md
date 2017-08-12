# Sensor network with Android

![architecture](./doc/architecture.jpg)

## Background and motivation

Once I used RasPi/Arduino for IoT prototyping, ending up with unsatisfying results: complicated physical wiring problems.

I stopped using RasPi/Arduino, and I started thinking of a combination of [PIC16F1-based sensor network](https://github.com/araobp/sensor-network) and Android to simplify physical wiring.

## Android apps

### Use case: bicycle

I develop in-bicycle network using the output from [sensor-network](https://github.com/araobp/sensor-network). It is like a cheap version of [CANopen](https://www.can-cia.org/canopen/) :-)

### CLI Activity

![screenshot_cli](./doc/screenshot_cli.png)

### Cycling Activity

![screenshot_cycling](./doc/screenshot_cycling.png)

### Technical requirements

- Very low power consumption
- Cheap
- Show current speed, acceleration, temperature and humidity on a character LCD
- Save time-series data (speed, temperature, humidity, acceleration and location) onto Android smartphone

### Thing: my bicycle

![network](./doc/network.jpg)

## Development tools

### Programing language
- [Kotlin](https://kotlinlang.org/)

### IDE
- [Android Studio](https://developer.android.com/studio/index.html)

### Libraries
- [Android Java D2XX driver](http://www.ftdichip.com/Drivers/D2XX.htm)
- [EventBus](http://greenrobot.org/eventbus/)

## Links
- [My smart phone: ASUS ZenFone Lazer](https://www.asus.com/Phone/ZenFone-2-Laser-ZE500KL/)
- [Dragon board (Quallcomm)](https://developer.qualcomm.com/hardware/dragonboard-410c)
- [CANopen](https://www.can-cia.org/canopen/)
