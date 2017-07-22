package jp.arao.iot.edge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

// IoT gateway for bicycle
public class MainActivity extends ReadListener {

    private static final String TAG = "CLI";
    private static final String DELIMETER = "\n";
    private static final int DEFAULT_BAUDRATE = 9600;  // 9600kbps
    private static final int SCHEDULER_BAUDRATE = 115200;  // 115200kbps

    private FtdiDevice mFtdiDevice = null;

    private TextView mTextView = null;
    private EditText mEditText = null;
    private Button mButtonOpen = null;
    private Button mButtonWrite = null;
    private Button mButtonClose = null;
    private CheckBox mCheckBox = null;
    private Switch mSwitch = null;

    private int baudrate() {
        if (mCheckBox != null) {
            return mCheckBox.isChecked() ? DEFAULT_BAUDRATE : SCHEDULER_BAUDRATE;
        } else {
            return SCHEDULER_BAUDRATE;
        }
    }

    private void updateView(boolean on) {
        if(on) {
            mButtonOpen.setEnabled(false);
            mButtonWrite.setEnabled(true);
            mButtonClose.setEnabled(true);
        } else {
            mButtonOpen.setEnabled(true);
            mButtonWrite.setEnabled(false);
            mButtonClose.setEnabled(false);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mFtdiDevice.open(baudrate());
    }

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                mFtdiDevice.open(baudrate());
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                mFtdiDevice.close();
                updateView(false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.textViewRead);
        mEditText = (EditText) findViewById(R.id.editTextWrite);

        mButtonOpen = (Button) findViewById(R.id.buttonOpen);
        mButtonWrite = (Button) findViewById(R.id.buttonWrite);
        mButtonClose = (Button) findViewById(R.id.buttonClose);

        mCheckBox = (CheckBox) findViewById(R.id.checkBoxBaudrate);
        mSwitch = (Switch) findViewById(R.id.switchStart);

        updateView(false);

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mFtdiDevice != null) {
                    if (isChecked) {
                        mFtdiDevice.write(Protocol.STA);
                    } else {
                        mFtdiDevice.write(Protocol.STP);
                    }
                }
            }
        });

        mFtdiDevice = new FtdiDevice(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);
    }

    public void onClickOpen(View v) {
        boolean update = false;
        update = mFtdiDevice.open(baudrate());
        updateView(update);
    }

    public void onClickWrite(View v) {
        String writeString = mEditText.getText().toString().toUpperCase() + DELIMETER;
        mFtdiDevice.write(writeString);
        mEditText.setText("");
    }

    public void onRead(String message) {
        mTextView.append(message);
    }

    public void onClickClose(View v) {
        mFtdiDevice.close();
        updateView(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFtdiDevice.stop();
        unregisterReceiver(mUsbReceiver);
    }
}
