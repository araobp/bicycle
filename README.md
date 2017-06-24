# Bicycle

I develop in-bicycle network.

## Requirements

- Show current speed, temperature and humidity on character LCD
- Save time-series data (speed, temperature, humidty, acceleration and location) onto EEPROM (256K bytes)

## Network

```
                                    (       )
 [Controller]--UART/USB--[IoT GW]--(  Cloud  )
      |                             (       )
      |                                                                                                
    --+---+-----------------+-----------------------+-------------------+----------------+----------+--- I2C bus
          |                 |                       |                   |                |          | 
   [hall sensor]  [temp/humid sensor 1]   [temp/humid sensor 2]   [Accelerometer]      [GPS]     [EEPROM]
    Front wheel       under saddle             on handlebar         front spoke    on handlebar   logging

```
