package com.znergy.flickrbrowser;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

// enum will contain all the states that the class will have
//enum DownloadStatus { IDLE, PROCESSING, NOT_INITIALISED, FAILED_OR_EMPTY, OK};


class GetRawDataWithNotes extends AsyncTask<String, Void, String> {
    private static final String TAG = "GetRawData";

    // track the status of our download
    private DownloadStatus downloadStatus;

    // we need to link our MainActivity so we can call the onDownloadComplete method
    // this is commonly called callback, but really it's just an instance of MainActivity
    private onDownloadComplete callback;

    // we implement an interface for the onDownloadComplete method to ensure
    // that the method will be called in our GetRawData class
    interface onDownloadComplete {
        void onDownloadComplete(String data, DownloadStatus status);
    }

    // constructor sets the default status of our AsyncTask (as idle)
    // implement an interface which is now what is being referred to as callback
    public GetRawDataWithNotes(onDownloadComplete callback) {
        this.downloadStatus = DownloadStatus.IDLE;
        this.callback = callback;
    }

    // this will run after the execute method
    // so we call the onDownloadComplete method in our MainActivity passing our downloaded data
    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: parameter = " + s);

        // check for null
        if(callback != null) {
            callback.onDownloadComplete(s, downloadStatus);
        }

        Log.d(TAG, "onPostExecute: finished");
    }

    @Override
    protected String doInBackground(String... params) {
        // connection and reader are required
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        // there should be a url being passed as params (this catches errors)
        if(params == null) {
            downloadStatus = DownloadStatus.NOT_INITIALISED;
            return null;
        }

        // this is where the actual api call happens (try/catch to prevent crashing)
        try {
            // change status to processing and grab the url being passed to doInBackground
            downloadStatus = DownloadStatus.PROCESSING;

            // there is only one parameter being passed, the url (hint: params[0])
            URL url = new URL(params[0]);

            // configuring the connection (get request, open connection and connect)
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // logging the response code for the http request (200 = ok, 400 not so much)
            int response = connection.getResponseCode();
            Log.d(TAG, "doInBackground: response code = " + response);

            // contain data we are getting ultimately
            StringBuilder result = new StringBuilder();

            // reader is what allows us to read the input stream
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            // this block will loop through the data until there is no data left
            // reader.readLine() is reading the line and adding it to our result
            /***** Original Reading of Input Stream *****/
            String line;
            while(null != (line = reader.readLine())) {
                result.append(line).append("\n");
            }

            // this will also work, and is used a lot. It has a better scope for the line variable
            // Since we don't have to define 'String line' outside our loop like the original
            /***** Alternative Reading of Input Stream *****/
            /*
            for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                result.append(line).append("\n");
            }
            */

            // finally after reading the input stream and appending it to our result
            // we return the result as a string
            downloadStatus = DownloadStatus.OK;
            return result.toString();

        } catch (MalformedURLException e) {
            Log.e(TAG, "doInBackground: Invalid URL " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "doInBackground: IO Exception reading data: " + e.getMessage());
        } catch (SecurityException e) {
            Log.e(TAG, "doInBackground: Security Exception. Needs permission " + e.getMessage());
        } finally {
            // this block of code executes after everything else
            if(connection != null) {
                // disconnect connection
                connection.disconnect();
            }
            try {
                // close input stream
                if(reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "doInBackground: Error closing Input Stream" + e.getMessage());
            }
        }

        downloadStatus = DownloadStatus.FAILED_OR_EMPTY;
        return null;
    }
}