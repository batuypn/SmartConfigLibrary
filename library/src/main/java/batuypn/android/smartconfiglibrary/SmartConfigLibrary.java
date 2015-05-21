package batuypn.android.smartconfiglibrary;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.integrity_project.smartconfiglib.SmartConfig;
import com.integrity_project.smartconfiglib.SmartConfigListener;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@EBean
public class SmartConfigLibrary {

    int runTime;
    boolean waitForScanFinish = false;
    boolean foundNewDevice = false;

    @Bean
    MDnsHelper mDnsHelper;

    MDnsCallbackInterface mDnsCallback;
    JSONArray devicesArray;
    JSONArray recentDevicesArray;

    byte[] freeData;
    SmartConfig smartConfig;
    SmartConfigListener smartConfigListener;

    @Pref
    SharedPreferencesInterface_ prefs;

    private Activity context;
    private Callback mCallback;
    public static final int RESULT_DEVICE_FOUND = 0;
    public static final int RESULT_DEVICE_NOT_FOUND = -1;

    public void registerListener(Callback mCallback){
        this.mCallback = mCallback;
    }

    public SmartConfigLibrary(Context context){
        this.context = (Activity) context;
        context.registerReceiver(scanFinishedReceiver, new IntentFilter(SmartConfigConstants.SCAN_FINISHED_BROADCAST_ACTION));
    }

    BroadcastReceiver scanFinishedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (waitForScanFinish || prefs.isSmartConfigActive().get()) {
                waitForScanFinish = false;
                lookForNewDevice();
            }
        }
    };

    @Background
    void runProgressBar() {
        runTime = 0;
        try {
            while (runTime < SmartConfigConstants.SC_RUNTIME) {
                if ((runTime > 0) && ((runTime % SmartConfigConstants.SC_MDNS_INTERVAL) == 0)) {
                    System.out.println("Pausing MDNS...");
                    pauseMDNS();
                }
                Thread.sleep(SmartConfigConstants.SC_PROGRESSBAR_INTERVAL);
                runTime += SmartConfigConstants.SC_PROGRESSBAR_INTERVAL;
            }
        } catch (InterruptedException e) {
        } finally {
            waitForScanFinish = false;
            if (!foundNewDevice) {
                notifyNotFoundNewDevice(); // haven't found new device
            }
            stopSmartConfig();
        }
    }

    public void startSmartConfig(String SSID, String passwordKey) {
        runProgressBar();
        foundNewDevice = false;
        byte[] paddedEncryptionKey;
        String gateway = NetworkUtil.getGateway(context);
        paddedEncryptionKey = null;
        freeData = new byte[1];
        freeData[0] = 0x03;
        smartConfig = null;
        smartConfigListener = new SmartConfigListener() {
            @Override
            public void onSmartConfigEvent(SmtCfgEvent event, Exception e) {}
        };
        try {
            smartConfig = new SmartConfig(smartConfigListener, freeData, passwordKey, paddedEncryptionKey, gateway, SSID, (byte) 0, "");
            smartConfig.transmitSettings();
            lookForNewDevice();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopSmartConfig() {
        try {
            smartConfig.stopTransmitting();
            runTime = SmartConfigConstants.SC_RUNTIME;
            mDnsHelper.stopDiscovery();
            Thread.sleep(SmartConfigConstants.JMDNS_CLOSE_TIME);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            prefs.isScanning().put(false);
            prefs.isSmartConfigActive().put(false);
            Intent intent = new Intent();
            intent.setAction(SmartConfigConstants.SCAN_FINISHED_BROADCAST_ACTION);
            context.sendBroadcast(intent);
        }
    }

    @Background
    public void pauseMDNS() {
        try {
            mDnsHelper.stopDiscovery();
            Thread.sleep(SmartConfigConstants.JMDNS_CLOSE_TIME);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            prefs.isScanning().put(false);
            Intent intent = new Intent();
            intent.setAction(SmartConfigConstants.SCAN_FINISHED_BROADCAST_ACTION);
            context.sendBroadcast(intent);
        }
    }

    @Background
    void lookForNewDevice() {
        try {
            devicesArray = new JSONArray(prefs.devicesArray().get()); //save whatever devices we found so far
            recentDevicesArray = new JSONArray(prefs.recentDevicesArray().get());
            if (prefs.isScanning().get()) { // if main activity is still scanning
                //((MainActivity)getActivity()).stopScanning();
                waitForScanFinish = true; // flag to indicate we are waiting for the main activity's scan to finish
                System.out.println("stopping scan on the main activity...");
            } else if (!waitForScanFinish) { // main activity is done scanning and we're not waiting for scan finish
                prefs.isScanning().put(true);
                prefs.isSmartConfigActive().put(true);
                mDnsCallback = new MDnsCallbackInterface() {

                    @Override
                    public void onDeviceResolved(JSONObject deviceJSON) {
                        if (isNewDevice(deviceJSON)) { // if this is a device we haven't already discovered
                            foundNewDevice = true;
                            recentDevicesArray.put(deviceJSON);
                            prefs.recentDevicesArray().put(recentDevicesArray.toString());// add the device to the new devices list and array
                            notifyFoundNewDevice(); // notify the user
                        }
                    }
                };
                mDnsHelper.init(context, mDnsCallback);
                mDnsHelper.startDiscovery();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isNewDevice(JSONObject deviceJSON) {
        try {
            for (int i=0; i<devicesArray.length(); i++) {
                if (devicesArray.getJSONObject(i).getString("host").equals(deviceJSON.getString("host"))) {
                    if (devicesArray.getJSONObject(i).getString("name").equals(deviceJSON.getString("name"))){
                        return false;
                    } else {
                        devicesArray = removeFromJSONArray(devicesArray, i);
                        prefs.devicesArray().put(devicesArray.toString());
                        return true;
                    }
                }
            }
            for (int i=0; i<recentDevicesArray.length(); i++) {
                if (recentDevicesArray.getJSONObject(i).getString("host").equals(deviceJSON.getString("host"))) {
                    if (recentDevicesArray.getJSONObject(i).getString("name").equals(deviceJSON.getString("name"))){
                        return false;
                    } else {
                        recentDevicesArray = removeFromJSONArray(recentDevicesArray, i);
                        prefs.recentDevicesArray().put(recentDevicesArray.toString());
                        return true;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    @UiThread
    public void notifyFoundNewDevice() {
        runTime = SmartConfigConstants.SC_RUNTIME; // stop progressbar and smartconfig
        context.unregisterReceiver(scanFinishedReceiver);
        mCallback.onSmartConfigResult(RESULT_DEVICE_FOUND);
        //TODO callback send device found
    }

    @UiThread
    public void notifyNotFoundNewDevice() {
        //TODO callback send not found
        mCallback.onSmartConfigResult(RESULT_DEVICE_NOT_FOUND);
    }

    public JSONArray removeFromJSONArray(JSONArray array, int index) {
        JSONArray result = new JSONArray();
        try {
            for (int i=0; i<array.length(); i++) {
                if (i != index) {
                    result.put(array.getJSONObject(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public interface Callback{
        void onSmartConfigResult(int result);
    }

}
