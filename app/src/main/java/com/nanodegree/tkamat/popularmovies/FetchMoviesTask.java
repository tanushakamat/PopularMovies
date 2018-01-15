package com.nanodegree.tkamat.popularmovies;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

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
 * Created by tnadkarn on 2/28/2017.
 */

public class FetchMoviesTask extends AsyncTask<Void, Void, String[]> {

    private final Activity context;
    final String POSTERSIZE = "w185";
    final String IMAGEBASEURL = "http://image.tmdb.org/t/p/";
    final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    public ArrayList<MovieData> movieDataList = new ArrayList<MovieData>();


    private CustomMovieAdapter movieAdapter;

    FetchMoviesTask(CustomMovieAdapter movieAdapter, Activity context)
    {
        this.movieAdapter = movieAdapter;
        this.context = context;
    }

    public String[] getMovieDataFromJson(String movieJsonStr) throws JSONException
    {
        Log.v(LOG_TAG, "FetchMoviesTask.getMovieDataFromJson");
        final String RESULTS = "results";

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray resultsArray = movieJson.getJSONArray(RESULTS);
        int resultsArrayLength = resultsArray.length();
        String[] posterPathArray = new String[resultsArrayLength];

        for(int i = 0; i < resultsArray.length(); i++) {
            JSONObject movieJSONObject = resultsArray.getJSONObject(i);
            posterPathArray[i] = movieJSONObject.getString(context.getString(R.string.string_poster_path));

           /* MovieData movieDataObject = new MovieData();
            movieDataList.add(movieDataObject);

            movieDataList.get(i).originalTitle = movieJSONObject.getString(context.getString(R.string.string_original_title));
            movieDataList.get(i).releaseDate = movieJSONObject.getString(context.getString(R.string.string_release_date));
            movieDataList.get(i).voteAverage = movieJSONObject.getString(context.getString(R.string.string_vote_average));
            movieDataList.get(i).overview = movieJSONObject.getString(context.getString(R.string.string_overview));
            movieDataList.get(i).id = movieJSONObject.getString("id");*/


            MovieData movieDataObject = new MovieData(movieJSONObject.getString(context.getString(R.string.string_original_title)),
                    movieJSONObject.getString(context.getString(R.string.string_release_date)),
                    movieJSONObject.getString(context.getString(R.string.string_vote_average)),
                    movieJSONObject.getString(context.getString(R.string.string_overview)),
                    movieJSONObject.getString("id"));

            movieDataList.add(movieDataObject);

        }



        return posterPathArray;
    }

    @Override
    protected String[] doInBackground(Void... Params)
    {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;



        try {
            // Construct the URL for the TMDB query
            Uri.Builder uri = new Uri.Builder();

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String popularityValue = context.getString(R.string.string_popularity_value);

            String sortingOrder = sharedPref.getString(context.getString(R.string.key_sort_order), popularityValue);
            final String TMDBBASEURL = "http://api.themoviedb.org/3/movie/" + sortingOrder + "?";

            final String APPID_PARAM = "api_key";

            if(BuildConfig.OPEN_WEATHER_MAP_API_KEY.equals("")) {
                Log.e(LOG_TAG, "Error: Please include tMDB api key in gradle.properties" );
            }

            Uri builtURI = Uri.parse(TMDBBASEURL).buildUpon().
                    appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY).build();
            URL url = new URL(builtURI.toString());

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
            String[] movieDataFromJson = getMovieDataFromJson(movieJsonStr);
            return movieDataFromJson;
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, e.getMessage(),e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result[])
    {
        if(result!=null)
        {
            movieAdapter.setMovieData(result);
            //movieAdapter.clear();
           /* for(String posterPath:result)
            {
                String completePosterURL = IMAGEBASEURL + POSTERSIZE + posterPath;
                movieAdapter.add(completePosterURL);
            }*/

        }
    }
}
