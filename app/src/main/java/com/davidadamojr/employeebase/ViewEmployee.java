package com.davidadamojr.employeebase;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

public class ViewEmployee extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewId;
    private TextView textViewFirstName;
    private TextView textViewLastName;
    private TextView textViewTitle;
    private TextView textViewSalary;

    private Button buttonEdit;
    private Button buttonDelete;

    private String id;

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

        getEmployee();
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
}
