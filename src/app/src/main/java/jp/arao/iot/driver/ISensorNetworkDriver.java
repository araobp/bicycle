package jp.arao.iot.driver;

public interface ISensorNetworkDriver {

    public void setReadListener(ReadListener readListener);

    public boolean open(int baudrate);

    public void write(String message);

    public void stop();

    public void close();

}
