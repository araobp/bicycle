package jp.araobp.iot.driver;

/*
* Sensor network driver interface
* */
public interface ISensorNetworkDriver {

    public void setReadListener(ReadListener readListener);

    public boolean open(int baudrate);

    public void write(String message);

    public void stop();

    public void close();

}
