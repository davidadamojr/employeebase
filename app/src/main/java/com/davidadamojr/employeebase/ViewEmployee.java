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
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static android.os.SystemClock.elapsedRealtime;

public class ViewEmployee extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewId;
    private TextView textViewFirstName;
    private TextView textViewLastName;
    private TextView textViewTitle;
    private TextView textViewSalary;

    private Button buttonEdit;
    private Button buttonDelete;

    private String id;

    private BroadcastReceiver refreshReceiver;
    private BroadcastReceiver contextReceiver;

    private static final String POLL_ACTION = "com.davidadamojr.employeebase.action.DETAIL";
    private static final String POLL_EXTRA = "com.davidadamojr.employeebase.extra.ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_employee);

        Intent intent = getIntent();

        id = intent.getStringExtra(Config.EMP_ID);

        textViewId = (TextView) findViewById(R.id.textViewId);
        textViewFirstName = (TextView) findViewById(R.id.textViewFirstName);
        textViewLastName = (TextView) findViewById(R.id.textViewLastName);
        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        textViewSalary = (TextView) findViewById(R.id.textViewSalary);

        buttonEdit = (Button) findViewById(R.id.buttonEdit);
        buttonDelete = (Button) findViewById(R.id.buttonDelete);

        buttonEdit.setOnClickListener(this);
        buttonDelete.setOnClickListener(this);

        textViewId.setText(id);

        getSupportActionBar().setTitle("View Employee");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        refreshReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String jsonStr = intent.getStringExtra("response");
                showEmployee(jsonStr);
            }
        };
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(POLL_ACTION);
        registerReceiver(refreshReceiver, iFilter);

        getEmployee();

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
        Map<String, Integer> batteryDetails = Utils.getBatteryDetails();

        boolean isWifi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

        // battery level greater than 50% is OK
        boolean batteryOk = batteryDetails.get("level") > 50;

        // batteryPlugged constant greater than 0 indicates it is connected to power source
        boolean batteryPlugged = batteryDetails.get("plugged") > 0;

        //TODO: refactor
        if (isWifi && batteryOk && batteryPlugged) {
            // poll every five minutes
            // setPoll(15000);
            setPoll(300000);
        } else if (isWifi && batteryOk && !batteryPlugged) {
            // poll every ten minutes
            // setPoll(15000);
            setPoll(600000);
        }

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
        Intent pollIntent = new Intent(ViewEmployee.this, PollService.class);
        pollIntent.setAction(POLL_ACTION);
        pollIntent.putExtra(POLL_EXTRA, id);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pIntent = PendingIntent.getService(ViewEmployee.this, 0, pollIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long triggerTime = elapsedRealtime() + interval;
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, triggerTime, interval, pIntent);
    }

    private void getEmployee() {
        class GetEmployee extends AsyncTask<Void,Void,String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(ViewEmployee.this, "Fetching...", "Wait...", false, false);
            }

            @Override
            protected void onPostExecute(String str) {
                super.onPostExecute(str);
                loading.dismiss();
                showEmployee(str);
            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler requestHandler = new RequestHandler();
                String str = requestHandler.sendGetRequestParam(Config.URL_GET_EMP, id);
                return str;
            }
        }

        GetEmployee ge = new GetEmployee();
        ge.execute();
    }

    private void showEmployee(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray result = jsonObject.getJSONArray(Config.TAG_JSON_ARRAY);
            JSONObject employee = result.getJSONObject(0);

            String fname = employee.getString(Config.TAG_FNAME);
            String lname = employee.getString(Config.TAG_LNAME);
            String title = employee.getString(Config.TAG_TITLE);
            String salary = employee.getString(Config.TAG_SAL);

            textViewFirstName.setText(fname);
            textViewLastName.setText(lname);
            textViewTitle.setText(title);
            textViewSalary.setText(salary);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void deleteEmployee() {
        class DeleteEmployee extends AsyncTask<Void,Void,String> {

            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(ViewEmployee.this, "Updating...", "Wait...", false, false);
            }

            @Override
            protected void onPostExecute(String str) {
                super.onPostExecute(str);
                loading.dismiss();
                Toast.makeText(ViewEmployee.this, str, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler requestHandler = new RequestHandler();
                String str = requestHandler.sendGetRequestParam(Config.URL_DELETE_EMP, id);
                return str;
            }
        }

        if (Utils.isOnline()) {
            DeleteEmployee de = new DeleteEmployee();
            de.execute();
            startActivity(new Intent(ViewEmployee.this, AllEmployees.class));
        } else {
            showInternetPrompt();
        }
    }

    private void confirmDeleteEmployee() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure you want to delete this employee?");

        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteEmployee();
                    }
                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onClick(View v) {
        if (v == buttonEdit) {
            if (Utils.isOnline()) {
                Intent intent = new Intent(this, EditEmployee.class);
                intent.putExtra(Config.EMP_ID, id);
                startActivity(intent);
            } else {
                showInternetPrompt();
            }
        }

        if (v == buttonDelete) {
            confirmDeleteEmployee();
        }
    }

    public void showInternetPrompt() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Unable to process your request. Please check your internet" +
                " connection and try again!");

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

        cancelPoll();
    }

    public void cancelPoll() {
        // cancel alarm manager
        Intent pollIntent = new Intent(ViewEmployee.this, PollService.class);
        pollIntent.setAction(POLL_ACTION);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pIntent = PendingIntent.getService(ViewEmployee.this, 0, pollIntent, PendingIntent.FLAG_NO_CREATE);
        if (pIntent != null) {
            alarmManager.cancel(pIntent);
            pIntent.cancel();
        }
    }
}
