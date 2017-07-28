package jp.arao.iot.driver;

import android.app.Activity;

/*
* Sends data from sensor network to Activity.
* */
public abstract class ReadListener extends Activity {

    public abstract void onRead(String output);

}
