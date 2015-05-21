# SmartConfigLibrary
SmartConfig Android Library for CC3200

To get it, add the following text to your build file:
```
<repositories {
  maven {
    url "https://jitpack.io"
	}
}

dependencies {
  compile 'com.github.batuypn:SmartConfigLibrary:v1.0.1'
}
```

Sample usage:
```
public class MainActivity extends Activity implements SmartConfigLibrary_.Callback{
    private SmartConfigLibrary_ smartConfigLibrary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smartConfigLibrary = SmartConfigLibrary_.getInstance_(this);
        smartConfigLibrary.registerListener(this);
    }

    //button onClick
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
```
