package jp.arao.iot.edge;

import android.app.Activity;

public abstract class ReadListener extends Activity {

    public abstract void onRead(String output);

}
