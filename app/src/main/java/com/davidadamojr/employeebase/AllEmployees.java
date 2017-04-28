package com.davidadamojr.employeebase;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

public class AllEmployees extends Activity implements ListView.OnItemClickListener {

    private ListView listView;

    private static final String POLL_ACTION = "com.davidadamojr.employeebase.action.ALL";

    private BroadcastReceiver refreshReceiver;
    private BroadcastReceiver contextReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_employees);

        getActionBar().setTitle("All Employees");

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        registerReceivers();

        if (Utils.isOnline()) {
            getJSON();

            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
            Map<String, Integer> batteryDetails = Utils.getBatteryDetails();

            boolean isWifi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

            // battery level greater than 50% is OK
            boolean batteryOk = batteryDetails.get("level") > 50;

            // batteryPlugged constant greater than 0 indicates it is connected to power source
            boolean batteryPlugged = batteryDetails.get("plugged") > 0;

            // refactor
            if (isWifi && batteryOk && batteryPlugged) {
                // poll every five minutes
                setPoll(300000);
                // setPoll(15000);
            } else if (isWifi && batteryOk && !batteryPlugged) {
                // poll every ten minutes
                setPoll(600000);
                // setPoll(15000);
            }

            // do not poll
        } else {
            showInternetPrompt();
        }
    }

    private void registerReceivers() {
        refreshReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String jsonStr = intent.getStringExtra("response");
                retrieveEmployees(jsonStr);
            }
        };
        IntentFilter refreshFilter = new IntentFilter();
        refreshFilter.addAction(POLL_ACTION);
        registerReceiver(refreshReceiver, refreshFilter);

        contextReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Map<String, Integer> batteryDetails = Utils.getBatteryDetails();
                boolean batteryOk = batteryDetails.get("level") > 50;
                boolean batteryPlugged = batteryDetails.get("plugged") > 0;
                if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                    if (Utils.isOnline()) {
                        // battery level greater than 50% is OK
                        if (batteryOk && batteryPlugged) {
                            setPoll(300000);
                        } else if (batteryOk && !batteryPlugged) {
                            setPoll(600000);
                        }
                    } else {
                        cancelPoll();
                    }
                } else if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
                    cancelPoll();
                } else if (intent.getAction().equals(Intent.ACTION_BATTERY_OKAY)) {
                    if (Utils.isOnline()) {
                        if (batteryOk && batteryPlugged) {
                            setPoll(300000);
                        } else if (batteryOk && !batteryPlugged) {
                            setPoll(600000);
                        }
                    }
                }
            }
        };
        IntentFilter contextFilter = new IntentFilter();
        contextFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        contextFilter.addAction(Intent.ACTION_BATTERY_LOW);
        contextFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        contextFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        contextFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(contextReceiver, contextFilter);
    }

    private void setPoll(long interval) {
        Intent pollIntent = new Intent(AllEmployees.this, PollService.class);
        pollIntent.setAction(POLL_ACTION);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pIntent = PendingIntent.getService(AllEmployees.this, 0, pollIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long triggerTime = elapsedRealtime() + interval;
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, triggerTime, interval, pIntent);
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

        if (contextReceiver != null) {
            unregisterReceiver(contextReceiver);
            contextReceiver = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        cancelPoll();
    }

    private void cancelPoll() {
        // cancel alarm manager
        Intent pollIntent = new Intent(AllEmployees.this, PollService.class);
        pollIntent.setAction(POLL_ACTION);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pIntent = PendingIntent.getService(AllEmployees.this, 0, pollIntent, PendingIntent.FLAG_NO_CREATE);
        if (pIntent != null) {
            alarmManager.cancel(pIntent);
            pIntent.cancel();
        }
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
