package com.davidadamojr.employeebase;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class PollService extends IntentService {

    private static final String ACTION_ALL = "com.davidadamojr.employeebase.action.ALL";
    private static final String ACTION_DETAIL = "com.davidadamojr.employeebase.action.DETAIL";
    private static final String EXTRA_ID = "com.davidadamojr.employeebase.extra.ID";

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public PollService() {
        super("PollService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_ALL.equals(action)) {
                handleActionAll();
            } else if (ACTION_DETAIL.equals(action)) {
                final String id = intent.getStringExtra(EXTRA_ID);
                handleActionDetail(id);
            }
        }
    }

    protected void handleActionAll() {
        if (Utils.isOnline()) {
            RequestHandler requestHandler = new RequestHandler();
            String jsonStr = requestHandler.sendGetRequest(Config.URL_GET_ALL);
            Intent responseIntent = new Intent();
            responseIntent.setAction(ACTION_ALL);
            responseIntent.putExtra("response", jsonStr);
            sendBroadcast(responseIntent);
        } else {
            // display toast from service
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "No internet connection.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    protected void handleActionDetail(String id) {
        if (Utils.isOnline()) {
            RequestHandler requestHandler = new RequestHandler();
            String jsonStr = requestHandler.sendGetRequestParam(Config.URL_GET_EMP, id);
            Intent responseIntent = new Intent();
            responseIntent.setAction(ACTION_DETAIL);
            responseIntent.putExtra("response", jsonStr);
            sendBroadcast(responseIntent);
        } else {
            mHandler.post(new Runnable() {
               @Override
               public void run() {
                   Toast.makeText(getApplicationContext(), "No internet connection.",
                           Toast.LENGTH_SHORT).show();
               }
            });
        }
    }
}
