package com.davidadamojr.employeebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ViewEmployee extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextId;
    private EditText editTextFirstName;
    private EditText editTextLastName;
    private EditText editTextTitle;
    private EditText editTextSalary;

    private Button buttonUpdate;
    private Button buttonDelete;

    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_employee);

        Intent intent = getIntent();

        id = intent.getStringExtra(Config.EMP_ID);

        editTextId = (EditText) findViewById(R.id.editTextId);
        editTextFirstName = (EditText) findViewById(R.id.editTextFirstName);
        editTextLastName = (EditText) findViewById(R.id.editTextLastName);
        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextSalary = (EditText) findViewById(R.id.editTextSalary);

        buttonUpdate = (Button) findViewById(R.id.buttonUpdate);
        buttonDelete = (Button) findViewById(R.id.buttonDelete);

        buttonUpdate.setOnClickListener(this);
        buttonUpdate.setOnClickListener(this);

        editTextId.setText(id);

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
    }
}
