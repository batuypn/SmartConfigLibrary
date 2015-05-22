# SmartConfigLibrary [![Maven Version](https://img.shields.io/github/release/batuypn/SmartConfigLibrary.svg?label=JitPack%20Maven)](https://jitpack.io/#batuypn/SmartConfigLibrary/v1.0.6)
SmartConfig Android Library for CC3200

To get it, add the following text to your build file:
```
repositories {
  maven {
    url "https://jitpack.io"
	}
}

dependencies {
  //change it with last version
  compile 'com.github.batuypn:SmartConfigLibrary:v1.0.6'
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
        //You should get these information from user (from Edittext etc.)
        smartConfigLibrary.startSmartConfig("SSID","password");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //in any kind of pause situation, stop broadcasting
        smartConfigLibrary.stopSmartConfig();
    }

    @Override
    public void onSmartConfigResult(int result) {
    	//the result callback after runTime(default 1 second)
    	//if no device, result is -1
    	//if a new device, result is 0
        Toast.makeText(this,Integer.toString(result),Toast.LENGTH_LONG).show();
    }
}
```
## Todos
- Add optional parameters (deviceName, key etc.) to startSmartConfig() function
- Add setRunTime(int millis) function to change the interval (default is 1 second)
- Add a callback function for any error

