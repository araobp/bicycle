package jp.arao.bicycle.edge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

// IoT gateway for bicycle
public class MainActivity extends ReadListener {

    private static final String DELIMETER = "\n";
    private static final int DEFAULT_BAUDRATE = 9600;  // 9600kbps
    private static final int SCHEDULER_BAUDRATE = 115200;  // 115200kbps

    private FtdiDevice ftdiDevice = null;

    private TextView tvRead = null;
    private EditText etWrite = null;
    private Button btOpen = null;
    private Button btWrite = null;
    private Button btClose = null;
    private CheckBox ckBox = null;

    private int baudrate() {
        int baudrate = SCHEDULER_BAUDRATE;
        if (ckBox != null) {
            baudrate = ckBox.isChecked() ? DEFAULT_BAUDRATE : SCHEDULER_BAUDRATE;
        }
        return baudrate;
    }

    private void updateView(boolean on) {
        if(on) {
            btOpen.setEnabled(false);
            btClose.setEnabled(true);
        } else {
            btOpen.setEnabled(true);
            btClose.setEnabled(false);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        ftdiDevice.openDevice(baudrate());
    }

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                ftdiDevice.openDevice(baudrate());
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                ftdiDevice.closeDevice();
                updateView(false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvRead = (TextView) findViewById(R.id.tvRead);
        etWrite = (EditText) findViewById(R.id.etWrite);

        btOpen = (Button) findViewById(R.id.btOpen);
        btWrite = (Button) findViewById(R.id.btWrite);
        btClose = (Button) findViewById(R.id.btClose);

        ckBox = (CheckBox) findViewById(R.id.ckBox);

        updateView(false);

        ftdiDevice = new FtdiDevice(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);
    }

    public void onClickOpen(View v) {
        boolean update = false;
        update = ftdiDevice.openDevice(baudrate());
        updateView(update);
    }

    public void onClickWrite(View v) {
        String writeString = etWrite.getText().toString().toUpperCase() + DELIMETER;
        ftdiDevice.write(writeString);
        etWrite.setText("");
    }

    public void onRead(String message) {
        tvRead.append(message);
    }

    public void onClickClose(View v) {
        ftdiDevice.closeDevice();
        updateView(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ftdiDevice.onDestroy();
        unregisterReceiver(mUsbReceiver);
    }
}
