# Bicycle

I develop in-bicycle network using the output from [sensor-network](https://github.com/araobp/sensor-network).

## Requirements

- Very low power consumption
- Cheap
- Show current speed, acceleration, temperature and humidity on a character LCD
- Save time-series data (speed, temperature, humidity, acceleration and location) onto Android smart phone.

## Thing: my bicycle

![bicycle](./doc/bicycle.jpg)

## Sensor network

![network](./doc/network.jpg)

```

                         [GPS]
                           |                                (       )
 [Scheduler]--UART/USB--[USB hub]--USB--[Android(IoT GW)]--(  Cloud  )
      |                                                     (       )
      |                                                                                                
    --+-------+-------------------+---------------+------------+--- I2C bus
              |                   |               |            |
   [temperature/humidity]  [accelerometer]    [LCD/LED]     [speed]
```

## IoT gateway

I use Android smart phone (ASUS ZenFone 2 Lazer) as IoT gateway for the sensor network. Android works as USB host with an USB cable supporting USB OTG.

![screenshot](./doc/Screenshot_20170701-014814.jpg)

## Development tools

### IDE
- 8bit MCU: [MPLAB-X](http://www.microchip.com/mplab/mplab-x-ide)
- Android Studio

### CAD
- PCB design: [Eagle](https://www.autodesk.com/products/eagle/overview)
- Encloure design: [FreeCAD](https://www.freecadweb.org/)

### Documentation
- PowerPoint

## References
- [Android USB host mode](http://relativelayout.hatenablog.com/entry/2016/08/12/085051)
