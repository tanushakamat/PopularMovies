package com.nanodegree.tkamat.popularmovies.reviews;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.nanodegree.tkamat.popularmovies.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by tnadkarn on 8/10/2017.
 */

public class FetchReviewsTask extends AsyncTask<String, Void, ArrayList<ReviewData>> {

    private final Activity context;
    final String LOG_TAG = FetchReviewsTask.class.getSimpleName();
    final String YOUTUBEBASEURL = "https://www.youtube.com/watch?v=";

    ArrayList<ReviewData> reviewDataList = new ArrayList<ReviewData>();

    private ReviewsAdapter mReviewsAdapter;





    public FetchReviewsTask(ReviewsAdapter reviewsAdapter, Activity context)
    {
        this.mReviewsAdapter = reviewsAdapter;
        this.context = context;
    }

    @Override
    protected ArrayList<ReviewData> doInBackground(String... parameters) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;



        try {
            // Construct the URL for the TMDB query
            Uri.Builder uri = new Uri.Builder();

            /*
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String popularityValue = context.getString(R.string.string_popularity_value);

            String sortingOrder = sharedPref.getString(context.getString(R.string.key_sort_order), popularityValue);*/
            String movieID = parameters[0];
            Log.v(LOG_TAG, "movieID = " + movieID);


            final String TMDBBASEURL = "http://api.themoviedb.org/3/movie/" + movieID + "/reviews?";

            final String APPID_PARAM = "api_key";

            if(BuildConfig.OPEN_WEATHER_MAP_API_KEY.equals("")) {
                Log.e(LOG_TAG, "Error: Please include tMDB api key in gradle.properties" );
            }

            Uri builtURI = Uri.parse(TMDBBASEURL).buildUpon().
                    appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY).build();
            URL url = new URL(builtURI.toString());
Log.v(LOG_TAG, "url = " + url.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                movieJsonStr = null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                movieJsonStr = null;
            }
            movieJsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            movieJsonStr = null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try{
            ArrayList<ReviewData> reviewDataListfromJson = getTrailerDataFromJson(movieJsonStr);
            return reviewDataListfromJson;
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, e.getMessage(),e);
            e.printStackTrace();
        }

        return null;
    }

    ArrayList<ReviewData> getTrailerDataFromJson(String reviewsJsonStr) throws JSONException
    {
        final String RESULTS = "results";

        Log.v(LOG_TAG, "reviewsJsonStr = " + reviewsJsonStr);
        JSONObject reviewsJson = new JSONObject(reviewsJsonStr);
        JSONArray resultsArray = reviewsJson.getJSONArray(RESULTS);
        int resultsArrayLength = resultsArray.length();

        //String[] reviewContent = new String[resultsArrayLength];

        for(int i = 0; i < resultsArray.length(); i++) {
            JSONObject reviewsJSONObject = resultsArray.getJSONObject(i);

            //reviewContent[i] = reviewsJSONObject.getString("content");
            String reviewContent = reviewsJSONObject.getString("content");
            String reviewAuthor = reviewsJSONObject.getString("author");
            String reviewUrl = reviewsJSONObject.getString("url");

            reviewDataList.add(new ReviewData(reviewContent, reviewAuthor, reviewUrl));

            //Log.v(LOG_TAG, "reviewContent[i] = " + reviewContent[i]);
        }

        return reviewDataList;
    }



    @Override
    protected void onPostExecute(ArrayList<ReviewData> reviewDataListfromJson) {
        if(reviewDataListfromJson!=null)
        {

            //Log.v(LOG_TAG,  ": " + trailerDataFromJson[0]);
                mReviewsAdapter.setReviewData(reviewDataListfromJson);
        }
    }
}
