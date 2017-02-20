package com.davidadamojr.employeebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Defining views
    private EditText editTextFirstName;
    private EditText editTextLastName;
    private EditText editTextTitle;
    private EditText editTextSalary;

    private Button buttonAdd;
    private Button buttonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextFirstName = (EditText) findViewById(R.id.editTextFirstName);
        editTextLastName = (EditText) findViewById(R.id.editTextLastName);
        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextSalary = (EditText) findViewById(R.id.editTextSalary);

        // setting listeners for the buttons
        buttonAdd.setOnClickListener(this);
        buttonView.setOnClickListener(this);
    }

    private void addEmployee() {

        final String firstName = editTextFirstName.getText().toString().trim();
        final String lastName = editTextLastName.getText().toString().trim();
        final String title = editTextTitle.getText().toString().trim();
        final String salary = editTextSalary.getText().toString().trim();

        class AddEmployee extends AsyncTask<Void,Void,String> {

            ProgressDialog loading;

            @Override
            protected void onPostExecute(String str) {
                super.onPostExecute(str);
                loading.dismiss();
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG).show();
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
            addEmployee();
        }

        if (v == buttonView) {
            startActivity(new Intent(this, AllEmployees.class));
        }
    }
}
