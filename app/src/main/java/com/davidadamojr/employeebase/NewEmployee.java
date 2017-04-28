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

import java.util.HashMap;

public class NewEmployee extends Activity implements View.OnClickListener {

    // Defining views
    private EditText editTextFirstName;
    private EditText editTextLastName;
    private EditText editTextTitle;
    private EditText editTextSalary;

    private Button buttonAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_employee);

        editTextFirstName = (EditText) findViewById(R.id.editTextFirstName);
        editTextLastName = (EditText) findViewById(R.id.editTextLastName);
        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextSalary = (EditText) findViewById(R.id.editTextSalary);

        buttonAdd = (Button) findViewById(R.id.buttonAdd);

        // setting listeners for the buttons
        buttonAdd.setOnClickListener(this);

        getActionBar().setTitle("New Employee");
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void addEmployee() {

        final String firstName = editTextFirstName.getText().toString().trim();
        final String lastName = editTextLastName.getText().toString().trim();
        final String title = editTextTitle.getText().toString().trim();
        final String salary = editTextSalary.getText().toString().trim();

        class AddEmployee extends AsyncTask<Void,Void,String> {

            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(NewEmployee.this, "Adding...", "Wait...", false, false);
            }

            @Override
            protected void onPostExecute(String str) {
                super.onPostExecute(str);
                loading.dismiss();
                startActivity(new Intent(NewEmployee.this, AllEmployees.class));
                Toast.makeText(NewEmployee.this, str, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected String doInBackground(Void... v) {
                HashMap<String, String> params = new HashMap<>();
                params.put(Config.KEY_EMP_FNAME, firstName);
                params.put(Config.KEY_EMP_LNAME, lastName);
                params.put(Config.KEY_EMP_TITLE, title);
                params.put(Config.KEY_EMP_SAL, salary);

                RequestHandler requestHandler = new RequestHandler();
                String response = requestHandler.sendPostRequest(Config.URL_ADD, params);
                return response;
            }
        }

        AddEmployee addEmployee = new AddEmployee();
        addEmployee.execute();
    }

    @Override
    public void onClick(View v) {
        if (v == buttonAdd) {
            if (Utils.isOnline()) {
                addEmployee();
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
