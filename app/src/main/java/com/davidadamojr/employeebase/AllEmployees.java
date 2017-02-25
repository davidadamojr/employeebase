package com.davidadamojr.employeebase;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class AllEmployees extends AppCompatActivity implements ListView.OnItemClickListener {

    private ListView listView;

    private String JSON_STRING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_employees);

        getSupportActionBar().setTitle("All Employees");

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);

        if (Utils.isOnline()) {
            getJSON();
        } else {
            showInternetPrompt();
        }
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

    private void showEmployees() {
        JSONObject jsonObject = null;
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        try {
            jsonObject = new JSONObject(JSON_STRING);
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
                list.add(employee);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ListAdapter adapter = new SimpleAdapter(AllEmployees.this, list, R.layout.list_item,
                new String[]{Config.TAG_NAME, Config.TAG_TITLE},
                new int[]{R.id.name, R.id.title});
        listView.setAdapter(adapter);
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
                JSON_STRING = str;
                showEmployees();
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
