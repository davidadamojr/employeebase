package com.davidadamojr.tester;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;

/**
 * Created by davidadamojr on 4/27/17.
 */

public class CoverageReceiver extends BroadcastReceiver {
    private static final String EXEC_FILE_PATH = "/mnt/sdcard/com.davidadamojr.employeebase/coverage.ec";
    private static final String TAG = "CoverageJacoco";
    private static final String BROADCAST_RECEIVED_MESSAGE = "EndJacocoBroadcast broadcast received!";
    private static final String EMMA_CLASS = "com.vladium.emma.rt.RT";
    private static final String EMMA_DUMP_METHOD = "dumpCoverageData";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.d(TAG, BROADCAST_RECEIVED_MESSAGE);
            Class.forName(EMMA_CLASS)
                    .getMethod(EMMA_DUMP_METHOD, File.class, boolean.class,
                            boolean.class)
                    .invoke(null, new File(EXEC_FILE_PATH), true,
                            false);
        } catch (Exception e) {

            Log.d(TAG, e.getMessage());
        }
    }
}