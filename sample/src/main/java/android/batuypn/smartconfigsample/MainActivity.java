package android.batuypn.smartconfigsample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import batuypn.android.smartconfiglibrary.SmartConfigLibrary_;

public class MainActivity extends Activity implements SmartConfigLibrary_.Callback{
    private SmartConfigLibrary_ smartConfigLibrary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smartConfigLibrary = SmartConfigLibrary_.getInstance_(this);
        smartConfigLibrary.registerListener(this);
    }

    public void startSmartConfig(View v){
        smartConfigLibrary.startSmartConfig("SSID","password");
    }

    @Override
    protected void onPause() {
        super.onPause();
        smartConfigLibrary.stopSmartConfig();
    }

    @Override
    public void onSmartConfigResult(int i) {
        Toast.makeText(this,Integer.toString(i),Toast.LENGTH_LONG).show();
    }
}
