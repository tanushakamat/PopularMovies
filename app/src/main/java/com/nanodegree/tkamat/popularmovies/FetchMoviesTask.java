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
    final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    public ArrayList<MovieData> movieDataList = new ArrayList<MovieData>();


    private CustomMovieAdapter movieAdapter;

    FetchMoviesTask(CustomMovieAdapter movieAdapter, Activity context) {
        this.movieAdapter = movieAdapter;
        this.context = context;
    }

    public String[] getMovieDataFromJson(String movieJsonStr) throws JSONException {
        //Log.v(LOG_TAG, "FetchMoviesTask.getMovieDataFromJson");
        final String RESULTS = "results";

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray resultsArray = movieJson.getJSONArray(RESULTS);
        int resultsArrayLength = resultsArray.length();
        String[] posterPathArray = new String[resultsArrayLength];

        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject movieJSONObject = resultsArray.getJSONObject(i);
            posterPathArray[i] = movieJSONObject.getString(context.getString(R.string.string_poster_path));

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
    protected String[] doInBackground(Void... Params) {

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String popularityValue = context.getString(R.string.string_popularity_value);
        String ratingValue = context.getString(R.string.string_rating_value);
        String favouritesValue = context.getString(R.string.string_favourites_value);
         ArrayList<String> mFavouriteMovieIds = MovieFragment.favouriteMovieIds;

        String sortingOrder = sharedPref.getString(context.getString(R.string.key_sort_order), popularityValue);

        if (BuildConfig.OPEN_WEATHER_MAP_API_KEY.equals("")) {
            Log.e(LOG_TAG, "Error: Please include tMDB api key in gradle.properties");
        }

        if (sortingOrder.equals(popularityValue) || sortingOrder.equals(ratingValue)) {

            final String TMDBBASEURLWITHSORTINGORDER = "http://api.themoviedb.org/3/movie/" + sortingOrder + "?";
            movieJsonStr = getJsonStr(TMDBBASEURLWITHSORTINGORDER);

            try {
                String[] movieDataFromJson = getMovieDataFromJson(movieJsonStr);
                return movieDataFromJson;
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        } else if (sortingOrder.equals(favouritesValue) && mFavouriteMovieIds.size() > 0) {
            //Movie specific api call example:
            //http://api.themoviedb.org/3/movie/321612?api_key=<api_key>

            ArrayList<String> favouriteMovieJsonStrings = new ArrayList<String>();

            for (String movieId : mFavouriteMovieIds) {

                final String TMDBBASEURLWITHMOVIEID = "http://api.themoviedb.org/3/movie/" + movieId + "?";
                movieJsonStr = getJsonStr(TMDBBASEURLWITHMOVIEID);

                favouriteMovieJsonStrings.add(movieJsonStr);
            }
            try {
                String[] movieDataFromJson = getFavouriteMovieDataFromJson(favouriteMovieJsonStrings);
                //Log.v(LOG_TAG, "movieDataFromJson.length = " + movieDataFromJson.length);
                return movieDataFromJson;
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

        }

        return null;
    }

    String getJsonStr(String tmdbBaseUrl)
    {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;
        try {

            final String APPID_PARAM = "api_key";

            Uri builtURI = Uri.parse(tmdbBaseUrl).buildUpon().
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
        } finally {
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

        return movieJsonStr;
    }

    public String[] getFavouriteMovieDataFromJson(ArrayList<String> favouriteMovieJsonStrings) throws JSONException {
        String[] posterPathArray = new String[favouriteMovieJsonStrings.size()];
        //Log.v(LOG_TAG, "favouriteMovieJsonStrings.length = " + favouriteMovieJsonStrings.size());

        int i = 0;
        for (String favouriteMovieJsonStr : favouriteMovieJsonStrings) {
            JSONObject favouriteMovieJson = new JSONObject(favouriteMovieJsonStr);
            posterPathArray[i] = favouriteMovieJson.getString(context.getString(R.string.string_poster_path));
            i++;

            MovieData movieDataObject = new MovieData(favouriteMovieJson.getString(context.getString(R.string.string_original_title)),
                    favouriteMovieJson.getString(context.getString(R.string.string_release_date)),
                    favouriteMovieJson.getString(context.getString(R.string.string_vote_average)),
                    favouriteMovieJson.getString(context.getString(R.string.string_overview)),
                    favouriteMovieJson.getString("id"));

            movieDataList.add(movieDataObject);
        }
        return posterPathArray;
    }

    @Override
    protected void onPostExecute(String result[]) {
        //if (result != null) {
            //Log.v(LOG_TAG, "Reached onPostExecute");
            if (movieAdapter != null) {
                movieAdapter.clear();
                movieAdapter.setMovieData(result);
            }
        //}
    }
}
