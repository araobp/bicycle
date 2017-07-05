package arao.jp.bicycle;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private UsbManager usbManager;
    private TextView textView;

    private static final String TAG = "Bicycle";
    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            //call method to set up device communication
                        }
                    }
                    else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

    private static final byte[] STA = "STA\n".getBytes();
    private static final int TIMEOUT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text_id);

        PendingIntent intent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(receiver, filter);

        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        textView.append("-- Device List --\n");
        while(deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            usbManager.requestPermission(device, intent);
            textView.append(device.toString());
            /*
            UsbInterface usbInterface = device.getInterface(0);
            UsbEndpoint endPoint = usbInterface.getEndpoint(0);
            UsbDeviceConnection connection = usbManager.openDevice(device);
            connection.claimInterface(usbInterface, true);
            connection.bulkTransfer(endPoint, STA, STA.length, TIMEOUT);
            */
        }
    }
}
