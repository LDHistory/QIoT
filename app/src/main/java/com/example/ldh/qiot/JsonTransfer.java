package com.example.ldh.qiot;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by LDH on 2017-11-03.
 */

public class JsonTransfer extends AsyncTask<String, Void, String> {

    static String jsonvalue;
    static boolean check;
    @Override
    protected String doInBackground(String... params) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try{
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .build();
            Request request = new Request.Builder()
                    .url(params[0])
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            //Log.d("value", client.newCall(request).execute().body().string());
            jsonvalue = response.body().string();
            check = true;
            Log.d("test", jsonvalue);
            response.body().close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return jsonvalue;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    public String getJson(){
        return jsonvalue;
    }
}