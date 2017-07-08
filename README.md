# Bicycle

I develop in-bicycle network using the output from [sensor-network](https://github.com/araobp/sensor-network).

## Requirements

- Very low power consumption
- Cheap
- Show current speed, acceleration, temperature and humidity on a character LCD
- Save time-series data (speed, temperature, humidity, acceleration and location) onto EEPROM.
- Transfer the data to the cloud via WiFi, when I have just arrived at my home.

## Thing: my bicycle

![bicycle](./doc/bicycle.jpg)

## Sensor network

![network](./doc/network.jpg)

```

  [EEPROM]
      | I2C                                               (       )
 [Scheduler]--UART--[ESP-WROOM-02(IoT GW)]---REST/WiFi---(  Cloud  )
      |                                                   (       )
      |                                                      
    --+-----------------+---------------------------+--------------+-- I2C bus
                        |                           |              |
           ---+---------+-------+---         ---+---+---+---       |
              |                 |               |       |          |
   [temperature/humidity][accelerometer]    [LCD/LED][speed]     [GPS]
```

## Development tools

### IDE
- PIC16F1829: [MPLAB-X](http://www.microchip.com/mplab/mplab-x-ide)
- ESP-WROOM-02: Arduino IDE

### CAD
- PCB design: [Eagle](https://www.autodesk.com/products/eagle/overview)
- Encloure design: [FreeCAD](https://www.freecadweb.org/)

### Documentation
- PowerPoint
- Atom
