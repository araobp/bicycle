package jp.araobp.iot.driver;

/*
* Sensor network driver interface
* */
public interface ISensorNetworkDriver {

    /*
    * sets callback method that receives messages one by one from Handler/Looper
    * */
    public void setReadListener(ReadListener readListener);

    /*
    * opens the device driver
    * */
    public boolean open(int baudrate);

    /*
    * writes a message to the device driver
    * */
    public void write(String message);

    /*
    * stops running the device driver
    * */
    public void stop();

    /*
    * closes the device driver
    * */
    public void close();

}
