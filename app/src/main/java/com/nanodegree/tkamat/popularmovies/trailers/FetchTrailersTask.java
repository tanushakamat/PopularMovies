package com.nanodegree.tkamat.popularmovies;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.nanodegree.tkamat.popularmovies.trailers.TrailersAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by tnadkarn on 8/10/2017.
 */

public class FetchTrailersTask extends AsyncTask<String, Void, String[]> {

    private final Activity context;
    final String LOG_TAG = FetchTrailersTask.class.getSimpleName();
    final String YOUTUBEBASEURL = "https://www.youtube.com/watch?v=";

    private TrailersAdapter mTrailersAdapter;



    public FetchTrailersTask(TrailersAdapter trailersAdapter, Activity context)
    {
        this.mTrailersAdapter = trailersAdapter;
        this.context = context;
    }

    @Override
    protected String[] doInBackground(String... parameters) {
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

            final String TMDBBASEURL = "http://api.themoviedb.org/3/movie/" + movieID + "/videos?";

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
            String[] trailerDataFromJson = getTrailerDataFromJson(movieJsonStr);
            return trailerDataFromJson;
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, e.getMessage(),e);
            e.printStackTrace();
        }

        return null;    }

    String[] getTrailerDataFromJson(String trailerJsonStr) throws JSONException
    {
        final String RESULTS = "results";

   /*     JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray resultsArray = movieJson.getJSONArray(RESULTS);
        int resultsArrayLength = resultsArray.length();
        String[] posterPathArray = new String[resultsArrayLength];

        for(int i = 0; i < resultsArray.length(); i++) {
            JSONObject movieJSONObject = resultsArray.getJSONObject(i);
            posterPathArray[i] = movieJSONObject.getString(context.getString(R.string.string_poster_path));

            MovieData movieDataObject = new MovieData();
            movieDataList.add(movieDataObject);

            movieDataList.get(i).originalTitle = movieJSONObject.getString(context.getString(R.string.string_original_title));
            movieDataList.get(i).releaseDate = movieJSONObject.getString(context.getString(R.string.string_release_date));
            movieDataList.get(i).voteAverage = movieJSONObject.getString(context.getString(R.string.string_vote_average));
            movieDataList.get(i).overview = movieJSONObject.getString(context.getString(R.string.string_overview));
        }

        return posterPathArray;*/
Log.v(LOG_TAG, "trailerJsonStr = " + trailerJsonStr);
        JSONObject trailerJson = new JSONObject(trailerJsonStr);
        JSONArray resultsArray = trailerJson.getJSONArray(RESULTS);
        int resultsArrayLength = resultsArray.length();

        String[] TrailerLinksArray = new String[resultsArrayLength];

        for(int i = 0; i < resultsArray.length(); i++) {
            JSONObject trailerJSONObject = resultsArray.getJSONObject(i);


            //reviewsDataList.get(i).trailerLinks = youtubeBaseUrl + trailerJSONObject.getString("key");
            TrailerLinksArray[i] = /*YOUTUBEBASEURL + */trailerJSONObject.getString("key");
            Log.v(LOG_TAG, "TrailerLinksArray[i] = " + TrailerLinksArray[i]);
        }

        return TrailerLinksArray;
        }



    @Override
    protected void onPostExecute(String[] trailerDataFromJson) {
        if(trailerDataFromJson!=null)
        {
            /*trailersAdapter.clear();
            for(String trailerLink:trailerDataFromJson)
            {
                String completeTrailerURL = YOUTUBEBASEURL + trailerLink;*/
            //Log.v(LOG_TAG,  ": " + trailerDataFromJson[0]);
                mTrailersAdapter.setTrailerData(trailerDataFromJson);
            //}

        }
    }
}
