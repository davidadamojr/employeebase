package com.davidadamojr.employeebase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class EditEmployee extends Activity implements View.OnClickListener {

    private EditText editTextUpdFname;
    private EditText editTextUpdLname;
    private EditText editTextUpdTitle;
    private EditText editTextUpdSalary;

    private Button buttonUpdate;

    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_employee);

        Intent intent = getIntent();

        id = intent.getStringExtra(Config.EMP_ID);

        editTextUpdFname = (EditText) findViewById(R.id.editTextUpdFname);
        editTextUpdLname = (EditText) findViewById(R.id.editTextUpdLname);
        editTextUpdTitle = (EditText) findViewById(R.id.editTextUpdTitle);
        editTextUpdSalary = (EditText) findViewById(R.id.editTextUpdSalary);

        buttonUpdate = (Button) findViewById(R.id.btnUpdate);

        getActionBar().setTitle("Edit Employee");
        getActionBar().setDisplayHomeAsUpEnabled(true);

        getEmployee();

        buttonUpdate.setOnClickListener(this);
    }

    private void getEmployee() {
        class GetEmployee extends AsyncTask<Void,Void,String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(EditEmployee.this, "Fetching...", "Wait...", false, false);
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

            editTextUpdFname.setText(fname);
            editTextUpdLname.setText(lname);
            editTextUpdTitle.setText(title);
            editTextUpdSalary.setText(salary);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateEmployee() {
        final String fname = editTextUpdFname.getText().toString().trim();
        final String lname = editTextUpdLname.getText().toString().trim();
        final String title = editTextUpdTitle.getText().toString().trim();
        final String salary = editTextUpdSalary.getText().toString().trim();

        class UpdateEmployee extends AsyncTask<Void,Void,String>{

            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(EditEmployee.this, "Updating...", "Wait...", false, false);
            }

            @Override
            protected void onPostExecute(String str) {
                super.onPostExecute(str);
                loading.dismiss();
                Toast.makeText(EditEmployee.this, str, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(Config.KEY_EMP_ID, id);
                hashMap.put(Config.KEY_EMP_FNAME, fname);
                hashMap.put(Config.KEY_EMP_LNAME, lname);
                hashMap.put(Config.KEY_EMP_TITLE, title);
                hashMap.put(Config.KEY_EMP_SAL, salary);

                RequestHandler requestHandler = new RequestHandler();

                String str = requestHandler.sendPostRequest(Config.URL_UPDATE_EMP, hashMap);

                return str;
            }
        }

        UpdateEmployee ue = new UpdateEmployee();
        ue.execute();
    }

    @Override
    public void onClick(View v) {
        if (v == buttonUpdate) {
            if (Utils.isOnline()) {
                updateEmployee();
            } else {
                showInternetPrompt();
            }
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
}
