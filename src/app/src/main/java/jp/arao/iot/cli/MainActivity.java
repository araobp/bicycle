package jp.arao.iot.cli;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/*
* Sensor Network CLI
*
* @see <a href="https://github.com/araobp/sensor-network/blob/master/doc/PROTOCOL.md">https://github.com/araobp/sensor-network/blob/master/doc/PROTOCOL.md</a>
* */
public class MainActivity extends ReadListener {

    public static final int DEFAULT_BAUDRATE = 9600;  // 9600kbps
    public static final int SCHEDULER_BAUDRATE = 115200;  // 115200kbps
    public static final int CMD_SEND_INTERVAL = 250;  // 250msec

    private int mBaudrate = 0;

    private static final String TAG = "CLI";

    private FtdiDevice mFtdiDevice = null;

    private TextView mTextView = null;
    private EditText mEditText = null;
    private Button mButtonOpen = null;
    private Button mButtonWrite = null;
    private CheckBox mCheckBox9600 = null;
    private Switch mSwitch = null;
    private TextView mTextViewScalerTitle = null;
    private TextView mTextViewScaler = null;
    private TextView mTextViewDevices = null;
    private List<TextView> mListSchedules = new ArrayList<>();

    private static String sButtonOpenOpen = "Open";
    private static String sButtonOpenClose = "Close";

    String mTimerScaler = "unknown";

    private void log(String message) {
        mTextView.append(message + "\n");
    }

    private boolean startCommunication() {
        boolean update = false;
        if (mFtdiDevice != null) {
            update = mFtdiDevice.open(mBaudrate);
            log(update ? "FTDI device connected": "Unable to connect FTDI device");
            try {
                Thread.sleep(CMD_SEND_INTERVAL);
                mFtdiDevice.write(Protocol.GET);
                Thread.sleep(CMD_SEND_INTERVAL);
                mFtdiDevice.write(Protocol.SCN);
                mFtdiDevice.write(Protocol.MAP);
                Thread.sleep(CMD_SEND_INTERVAL);
                mFtdiDevice.write(Protocol.RSC);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
        return update;
    }

    private void updateButtonText(boolean on) {
        if(on) {
            mButtonOpen.setText(sButtonOpenClose);
            mButtonWrite.setEnabled(true);
        } else {
            mButtonOpen.setText(sButtonOpenOpen);
            mButtonWrite.setEnabled(false);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        startCommunication();
    }

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                startCommunication();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                mFtdiDevice.close();
                updateButtonText(false);
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

        mCheckBox9600 = (CheckBox) findViewById(R.id.checkBoxBaudrate9600);

        mSwitch = (Switch) findViewById(R.id.switchStart);
        mTextViewScalerTitle = (TextView) findViewById(R.id.textViewScalerTitle);
        mTextViewScaler = (TextView) findViewById(R.id.textViewScaler);
        mTextViewScaler.setText(mTimerScaler);

        mTextViewDevices = (TextView) findViewById(R.id.textViewDevices);

        mListSchedules.add( (TextView) findViewById(R.id.textViewSchedule1) );
        mListSchedules.add( (TextView) findViewById(R.id.textViewSchedule2) );
        mListSchedules.add( (TextView) findViewById(R.id.textViewSchedule3) );
        mListSchedules.add( (TextView) findViewById(R.id.textViewSchedule4) );
        mListSchedules.add( (TextView) findViewById(R.id.textViewSchedule5) );
        mListSchedules.add( (TextView) findViewById(R.id.textViewSchedule6) );
        mListSchedules.add( (TextView) findViewById(R.id.textViewSchedule7) );

        updateButtonText(false);

        mButtonOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mButtonOpen.getText().equals(sButtonOpenOpen)) {
                    updateButtonText(startCommunication());
                } else {
                    mFtdiDevice.close();
                    updateButtonText(false);
                }
            }
        });

        mButtonWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String writeString = mEditText.getText().toString().toUpperCase();
                mFtdiDevice.write(writeString);
                mEditText.setText("");
            }
        });

        mBaudrate = mCheckBox9600.isChecked() ? DEFAULT_BAUDRATE : SCHEDULER_BAUDRATE;

        mCheckBox9600.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mCheckBox9600.isChecked()) {
                    mBaudrate = DEFAULT_BAUDRATE;
                } else {
                    mBaudrate = SCHEDULER_BAUDRATE;
                }
                Log.d(TAG, Integer.toString(mBaudrate));
            }
        });

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mFtdiDevice != null) {
                    if (isChecked) {
                        log("Switch on");
                        mFtdiDevice.write(Protocol.STA);
                    } else {
                        mFtdiDevice.write(Protocol.STP);
                        log("Switch off");
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

    public void onRead(String message) {
        log(message);
        if (message.startsWith("$")) {
            String[] response = message.split(":");
            switch (response[1]) {
                case Protocol.GET:
                    mTimerScaler = response[2];
                    mTextViewScaler.setText(mTimerScaler);
                    break;
                case Protocol.MAP:
                    mTextViewDevices.setText("");
                    mTextViewDevices.append(response[2]);
                    break;
                case Protocol.RSC:
                    String[] schs = response[2].split("\\|");
                    for (int i=0;i<schs.length;i++) {
                        mListSchedules.get(i).setText(schs[i]);
                    }
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFtdiDevice.stop();
        unregisterReceiver(mUsbReceiver);
    }
}
