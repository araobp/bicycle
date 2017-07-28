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
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import jp.arao.iot.driver.imp.SensorNetworkDriverImpl;
import jp.arao.iot.driver.ISensorNetworkDriver;
import jp.arao.iot.driver.ReadListener;
import jp.arao.iot.driver.imp.SensorNetworkSimulator;
import jp.arao.iot.protocol.Protocol;

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

    private ISensorNetworkDriver mDriver = null;

    private TextView mTextView = null;
    private EditText mEditText = null;
    private Button mButtonOpen = null;
    private Button mButtonWrite = null;
    private ToggleButton mToggleButton = null;
    private CheckBox mCheckBoxBaudrate9600 = null;
    private CheckBox mCheckBoxSimualtor = null;
    private Switch mSwitch = null;
    private TextView mTextViewScaler = null;
    private TextView mTextViewDevices = null;
    private List<TextView> mListSchedules = new ArrayList<>();

    private static String sButtonOpenOpen = "Open";
    private static String sButtonOpenClose = "Close";

    private boolean mResponseLoggingEnabled = false;

    private boolean mOpened = false;
    private boolean mStarted = false;

    String mTimerScaler = "unknown";

    private void log(String message) {
        mTextView.append(message + "\n");
    }

    private boolean startCommunication() {
        boolean update = false;
        if (mCheckBoxSimualtor.isChecked()) {
            log("Initializing sensor network simulator");
            if (mDriver == null || mDriver instanceof SensorNetworkDriverImpl) {
                mDriver = new SensorNetworkSimulator();
            }
        } else {
            log("Initializing sensor network driver");
            if (mDriver == null || mDriver instanceof SensorNetworkSimulator) {
                mDriver = new SensorNetworkDriverImpl();
            }
        }
        if (mDriver != null) {
            mDriver.setReadListener(this);
            update = mDriver.open(mBaudrate);
            log(update ? "Sensor network connected": "Unable to connect sensor network");
            try {
                Thread.sleep(CMD_SEND_INTERVAL);
                mDriver.write(Protocol.GET);
                Thread.sleep(CMD_SEND_INTERVAL);
                mDriver.write(Protocol.SCN);
                mDriver.write(Protocol.MAP);
                Thread.sleep(CMD_SEND_INTERVAL);
                mDriver.write(Protocol.RSC);
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
        updateButtonText(startCommunication());
    }

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                updateButtonText(startCommunication());
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                mDriver.close();
                mSwitch.setChecked(false);
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
        mToggleButton = (ToggleButton) findViewById(R.id.toggleButton);

        mCheckBoxBaudrate9600 = (CheckBox) findViewById(R.id.checkBoxBaudrate9600);
        mCheckBoxSimualtor = (CheckBox) findViewById(R.id.checkBoxSimulator);

        mSwitch = (Switch) findViewById(R.id.switchStart);
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
                    mOpened = true;
                    if (mStarted) {
                        mSwitch.setChecked(true);
                    }
                } else {
                    mDriver.close();
                    mOpened = false;
                    updateButtonText(false);
                    if (mStarted) {
                        mSwitch.setChecked(false);
                    }
                }
            }
        });

        mButtonWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String writeString = mEditText.getText().toString().toUpperCase();
                mDriver.write(writeString);
                mEditText.setText("");
            }
        });

        mBaudrate = mCheckBoxBaudrate9600.isChecked() ? DEFAULT_BAUDRATE : SCHEDULER_BAUDRATE;

        mCheckBoxBaudrate9600.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mCheckBoxBaudrate9600.isChecked()) {
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
                if (mDriver != null) {
                    if (isChecked) {
                        log("Switch on");
                        mDriver.write(Protocol.STA);
                        mSwitch.setChecked(true);
                        mStarted = true;
                    } else {
                        log("Switch off");
                        if (mOpened) {
                            mDriver.write(Protocol.STP);
                            mStarted = false;
                        }
                        mSwitch.setChecked(false);
                    }
                }
            }
        });

        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {  // OFF
                    log("Logging disabled");
                    mResponseLoggingEnabled = false;
                } else {  // ON
                    log("Logging enabled");
                    mTextView.setText("");
                    mResponseLoggingEnabled = true;
                }

            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);
    }

    public void onRead(String message) {
        if (mResponseLoggingEnabled) {
            log(message);
        }
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
        mDriver.stop();
        unregisterReceiver(mUsbReceiver);
    }
}
