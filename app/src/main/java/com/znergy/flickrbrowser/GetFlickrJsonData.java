package com.znergy.flickrbrowser;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by administrator on 6/4/17.
 */

class GetFlickrJsonData implements GetRawData.OnDownloadComplete {
    private static final String TAG = GetFlickrJsonData.class.getSimpleName();

    private List<Photo> photoList = null; // list of photo objects
    private String baseURL; // https://flickr.com/...
    private String language; // multiple languages supported by flickr (en-en, de-de, etc)
    private boolean matchAll; // match all of our search? (true or false)

    // final variable has to be initialized or added to constructor!!
    private final OnDataAvailable callBack;

    interface OnDataAvailable {
        void onDataAvailable(List<Photo> data, DownloadStatus status);
    }

    public GetFlickrJsonData(OnDataAvailable callBack, String baseURL, String language, boolean matchAll) {
        Log.d(TAG, "GetFlickrJsonData: called");
        this.callBack = callBack;
        this.baseURL = baseURL;
        this.language = language;
        this.matchAll = matchAll;
    }

    public void executeOnSameThread(String searchCriteria) {
        Log.d(TAG, "executeOnSameThread: started");
        // creating a url with these parameters
        String destinationUri = createUri(searchCriteria, language, matchAll);

        GetRawData getRawData = new GetRawData(this);
        getRawData.execute(destinationUri);

        Log.d(TAG, "executeOnSameThread: ended");
    }

    private String createUri(String searchCriteria, String language, boolean matchAll) {
        Log.d(TAG, "createUri: starts");

        /*** Long form ***/
//        Uri uri = Uri.parse(baseURL);
//        Uri.Builder builder = uri.buildUpon();
//        builder = builder.appendQueryParameter("tags", searchCriteria);
//        builder = builder.appendQueryParameter("lang", language);
//        builder = builder.appendQueryParameter("tagmode", matchAll ? "ALL" : "ANY");
//        builder = builder.appendQueryParameter("format", "json");
//        builder = builder.appendQueryParameter("nojsoncallback", "1");
//        uri = builder.build();

        /*** Short Form ***/
        // constructing URL.. baseURL = "https://flickr.com/public_en"
        // after construction.. return = "https://flickr.com/public_en?tags=android,lollipop&tagmode=ALL&format=json&lang=en-en&nojsoncallback=1
        return Uri.parse(baseURL).buildUpon()
                .appendQueryParameter("tags", searchCriteria)
                .appendQueryParameter("lang", language)
                .appendQueryParameter("tagmode", matchAll ? "ALL" : "ANY")
                .appendQueryParameter("format", "json")
                .appendQueryParameter("nojsoncallback", "1")
                .build().toString();
    }

    @Override
    public void onDownloadComplete(String data, DownloadStatus status) {
        Log.d(TAG, "onDownloadComplete: started");
        /** data was downloaded successfully, create array of photo objects */
        if(status == DownloadStatus.OK) {
            photoList = new ArrayList<>();
            try {
                JSONObject jsonData = new JSONObject(data);
                JSONArray itemsArray = jsonData.getJSONArray("items");

                for(int i = 0; i < itemsArray.length(); i++) {
                    JSONObject jsonPhoto = itemsArray.getJSONObject(i);
                    String title = jsonPhoto.getString("title");
                    String author = jsonPhoto.getString("author");
                    String authorId = jsonPhoto.getString("author_id");
                    String tags = jsonPhoto.getString("tags");
                    String image = jsonPhoto.getJSONObject("media").getString("m");
                    String link = image.replaceFirst("_m.", "_b."); // searching link cut out _m and replace with _b (changing image size)

                    Photo photo = new Photo(title, author, authorId, link, tags, image);
                    photoList.add(photo);

                    Log.d(TAG, "onDownloadComplete: Photo Object: " + photo.toString());
                 }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "onDownloadComplete: Error processing json data" + e.getMessage());
                status = DownloadStatus.FAILED_OR_EMPTY;
            }
        }
        /** defensive programming (check if callback is null) */
        if(callBack != null) {
            /** Now inform the caller that processing is done - possibly returning null if
             * there was an error */
            callBack.onDataAvailable(photoList, status);
        }
        Log.d(TAG, "onDownloadComplete: ends");
    }
}

















