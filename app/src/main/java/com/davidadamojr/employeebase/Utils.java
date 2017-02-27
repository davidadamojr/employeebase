package com.davidadamojr.employeebase;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidadamojr on 2/24/17.
 */

public class Utils {
//    public static boolean isOnline() {
//        Runtime runtime = Runtime.getRuntime();
//        try {
//            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
//            int exitValue = ipProcess.waitFor();
//            return (exitValue == 0);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        return false;
//    }

    public static boolean isOnline() {
        ConnectivityManager connManager = (ConnectivityManager) EmployeeBase.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI && activeNetwork.isConnectedOrConnecting();
    }

    public static Map<String, Integer> getBatteryDetails() {
        Map<String, Integer> batteryDetails = new HashMap<>();
        Intent batteryIntent = EmployeeBase.getAppContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        batteryDetails.put("level", batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1));
        batteryDetails.put("plugged", batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1));

        return batteryDetails;
    }
}
