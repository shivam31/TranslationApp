package com.droidacid.translationapp;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends ActionBarActivity {

    EditText etTranslate;
    TextView tvTranslation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onTranslate(View view) {

        etTranslate = (EditText) findViewById(R.id.etTranslateText);

        if (!isEmpty(etTranslate)) {
            Toast.makeText(this, "Getting translations",
                    Toast.LENGTH_LONG).show();

            new SaveTheFeed().execute();
        } else {
            Toast.makeText(this, "Enter words to translate",
                    Toast.LENGTH_SHORT).show();
        }

    }

    protected boolean isEmpty(EditText translateText) {
        return translateText.getText().toString().trim().length() == 0;
    }


    // Allows you to perform background operations without locking up the user interface
    // until they are finished
    // The void part is stating that it doesn't receive parameters, it doesn't monitor progress
    // and it won't pass a result to onPostExecute

    // First Void - this is not going to receive any parameters when its is called
    // Second Void - its not going to monitor the progress of the task being performed
    // Third Void - doInBackground is not going to pass anything to onPostExecute()
    private class SaveTheFeed extends AsyncTask<Void, Void, Void> {

        String jsonString = "";

        String result = "";

        @Override
        protected Void doInBackground(Void... params) {

            performNetworkTasks();
            return null;
        }

        // Performs all the network related taks in this method
        protected void performNetworkTasks() {
            String translateUrl = "http://newjustin.com/translateit.php?action=translations&english_words=";

            String wordsToTranslate = etTranslate.getText().toString();

            // Replace spaces in the String that was entered with + so they can be passed
            // in a URL
            wordsToTranslate = wordsToTranslate.replace(" ", "+");
            // Client used to grab data from a provided URL
            HttpClient httpClient = new DefaultHttpClient(
                    new BasicHttpParams());

            // Provide the URL for the post request
            HttpPost httpPost = new HttpPost(translateUrl + wordsToTranslate);

            // Define that the data expected is in JSON format
            httpPost.setHeader("Content-type", "application/json");

            // Allows you to input a stream of bytes from the URL
            InputStream inputStream = null;

            try {

                // The client calls for the post request to execute and sends the results back
                HttpResponse response = httpClient.execute(httpPost);

                // Holds the message sent by the response
                HttpEntity entity = response.getEntity();

                // Get the content sent
                inputStream = entity.getContent();

                // A BufferedReader is used because it is efficient
                // The InputStreamReader converts the bytes into characters
                // My JSON data is UTF-8 so I read that encoding
                // 8 defines the input buffer size
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);

                StringBuilder sb = new StringBuilder();

                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                jsonString = sb.toString();

                getJsonData(jsonString);


            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            tvTranslation = (TextView) findViewById(R.id.tvTranslation);
            tvTranslation.setText(result);
        }

        protected void getJsonData(String jsonString) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONArray("translations");



                outputTranslations(jsonArray);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        protected void outputTranslations(JSONArray jsonArray) {

            Resources res = getResources();
            String languages[] = res.getStringArray(R.array.languages);

            try {

                for(int i = 0; i < jsonArray.length(); i++) {
                    JSONObject translationObject =
                        jsonArray.getJSONObject(i);
                        result = result + languages[i] +
                                " : " + translationObject.getString(languages[i]);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
