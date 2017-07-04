package arao.jp.bicycle;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private UsbManager usbManager;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text_id);
        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        textView.append("-- Device List --\n");
        for(String key: deviceList.keySet()) {
            UsbDevice device = deviceList.get(key);
            String device_name = device.toString();
            textView.append(device_name + "\n");
        }
    }
}
