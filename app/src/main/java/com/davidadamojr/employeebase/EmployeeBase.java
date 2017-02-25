package com.davidadamojr.employeebase;

import android.app.Application;
import android.content.Context;

/**
 * Created by davidadamojr on 2/25/17.
 */

public class EmployeeBase extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        EmployeeBase.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return EmployeeBase.context;
    }
}
