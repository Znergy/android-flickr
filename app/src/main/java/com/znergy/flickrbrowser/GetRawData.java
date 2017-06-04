package com.znergy.flickrbrowser;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

enum DownloadStatus { IDLE, PROCESSING, NOT_INITIALISED, FAILED_OR_EMPTY, OK};


class GetRawData extends AsyncTask<String, Void, String> {
    private static final String TAG = "GetRawData";

    private DownloadStatus downloadStatus;
    private MainActivity callback;

    public GetRawData(MainActivity callback) {
        this.downloadStatus = DownloadStatus.IDLE;
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: parameter = " + s);

        if(callback != null) {
            callback.onDownloadComplete(s, downloadStatus);
        }

        Log.d(TAG, "onPostExecute: finished");
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        if(params == null) {
            downloadStatus = DownloadStatus.NOT_INITIALISED;
            return null;
        }

        try {
            downloadStatus = DownloadStatus.PROCESSING;

            URL url = new URL(params[0]);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int response = connection.getResponseCode();
            Log.d(TAG, "doInBackground: response code = " + response);

            StringBuilder result = new StringBuilder();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while(null != (line = reader.readLine())) {
                result.append(line).append("\n");
            }

            downloadStatus = DownloadStatus.OK;
            return result.toString();

        } catch (MalformedURLException e) {
            Log.e(TAG, "doInBackground: Invalid URL " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "doInBackground: IO Exception reading data: " + e.getMessage());
        } catch (SecurityException e) {
            Log.e(TAG, "doInBackground: Security Exception. Needs permission " + e.getMessage());
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
            try {
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













