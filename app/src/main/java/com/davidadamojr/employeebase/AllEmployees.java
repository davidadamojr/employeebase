package com.davidadamojr.employeebase;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.SystemClock.elapsedRealtime;

public class AllEmployees extends AppCompatActivity implements ListView.OnItemClickListener {

    private ListView listView;

    private static final String POLL_ACTION = "com.davidadamojr.employeebase.action.ALL";
    private static final String REFRESH_ACTION = "com.davidadamojr.employeebase.action.REFRESH_ALL";

    private BroadcastReceiver refreshReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_employees);

        getSupportActionBar().setTitle("All Employees");

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);

        refreshReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String jsonStr = intent.getStringExtra("response");
                retrieveEmployees(jsonStr);
            }
        };
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(REFRESH_ACTION);
        registerReceiver(refreshReceiver, iFilter);

        if (Utils.isOnline()) {
            getJSON();

            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
            Map<String, Integer> batteryDetails = getBatteryDetails();

            boolean isWifi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

            // battery level greater than 50% is OK
            boolean batteryOk = batteryDetails.get("level") > 50;

            // batteryPlugged constant greater than 0 indicates it is connected to power source
            boolean batteryPlugged = batteryDetails.get("plugged") > 0;

            if (isWifi && batteryOk && batteryPlugged) {
                // poll every minute
                Intent pollIntent = new Intent(AllEmployees.this, AllEmployeeService.class);
                pollIntent.setAction(POLL_ACTION);
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                PendingIntent pIntent = PendingIntent.getService(AllEmployees.this, 0, pollIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                //long interval = 300000; // 5 minutes
                long interval = 10000;
                long triggerTime = elapsedRealtime() + interval;
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, triggerTime, interval + 5000, pIntent);
            } else if (isWifi && batteryOk && !batteryPlugged) {
                // poll every five minutes
                Intent pollIntent = new Intent(AllEmployees.this, AllEmployeeService.class);
                pollIntent.setAction(POLL_ACTION);
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                PendingIntent pIntent = PendingIntent.getService(AllEmployees.this, 0, pollIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                long interval = 600000; // 10 minutes
                long triggerTime = elapsedRealtime() + interval;
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, triggerTime, triggerTime, pIntent);
            }

            // do not poll
        } else {
            showInternetPrompt();
        }
    }

    public Map<String, Integer> getBatteryDetails() {
        Map<String, Integer> batteryDetails = new HashMap<>();
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        batteryDetails.put("level", batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1));
        batteryDetails.put("plugged", batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1));

        return batteryDetails;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.all_employees, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_employee:
                // user chose to add a new employee
                startActivity(new Intent(this, NewEmployee.class));
                return true;

            case R.id.action_refresh:
                if (Utils.isOnline()) {
                    getJSON();
                } else {
                    showInternetPrompt();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister broadcast receiver
        if (refreshReceiver != null) {
            unregisterReceiver(refreshReceiver);
            refreshReceiver = null;
        }

        // cancel alarm manager
        Intent pollIntent = new Intent(AllEmployees.this, AllEmployeeService.class);
        pollIntent.setAction(POLL_ACTION);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pIntent = PendingIntent.getService(AllEmployees.this, 0, pollIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pIntent);
    }

    public void showInternetPrompt() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Unable to connect to the Internet. Please check your internet" +
                " connection!");

        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialogBuilder.setCancelable(true);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void retrieveEmployees(String jsonStr) {
        List<HashMap<String, String>> employeeList = new ArrayList<HashMap<String,String>>();
        try {
            employeeList.clear();
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONArray result = jsonObject.getJSONArray(Config.TAG_JSON_ARRAY);

            for (int i=0; i < result.length(); i++) {
                JSONObject jo = result.getJSONObject(i);
                String id = jo.getString(Config.TAG_ID);
                String name = String.format("%s %s", jo.getString(Config.TAG_FNAME), jo.getString(Config.TAG_LNAME));
                String title = jo.getString(Config.TAG_TITLE);

                HashMap<String, String> employee = new HashMap<>();
                employee.put(Config.TAG_ID, id);
                employee.put(Config.TAG_NAME, name);
                employee.put(Config.TAG_TITLE, title);
                employeeList.add(employee);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SimpleAdapter employeeAdapter = new SimpleAdapter(AllEmployees.this, employeeList, R.layout.list_item,
                new String[]{Config.TAG_NAME, Config.TAG_TITLE},
                new int[]{R.id.name, R.id.title});
        listView.setAdapter(employeeAdapter);
    }

    private void getJSON() {
        class GetJSON extends AsyncTask<Void,Void,String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(AllEmployees.this, "Fetching Data", "Wait...", false, false);
            }

            @Override
            protected void onPostExecute(String str) {
                super.onPostExecute(str);
                loading.dismiss();
                retrieveEmployees(str);
            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler requestHandler = new RequestHandler();
                String str = requestHandler.sendGetRequest(Config.URL_GET_ALL);
                return str;
            }
        }

        GetJSON gj = new GetJSON();
        gj.execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (Utils.isOnline()) {
            Intent intent = new Intent(this, ViewEmployee.class);
            HashMap<String, String> map = (HashMap) parent.getItemAtPosition(position);
            String empId = map.get(Config.TAG_ID).toString();
            intent.putExtra(Config.EMP_ID, empId);
            startActivity(intent);
        } else {
            showInternetPrompt();
        }
    }
}
