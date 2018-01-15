package com.nanodegree.tkamat.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nanodegree.tkamat.popularmovies.data.MovieContract;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment implements CustomMovieAdapter.ListItemClickListener, LoaderManager.LoaderCallbacks<Cursor>{

    final String LOG_TAG = MovieFragment.class.getSimpleName();

    private CustomMovieAdapter mMovieAdapter;
    //private GridView gridView;
    private FetchMoviesTask fetchMoviesTask;
    RecyclerView mMoviesRecyclerView;



    private final static String COMPLETE_POSTER_PATH_KEY = "completePosterPathKey";
    private final static String MOVIE_DATA_OBJECT_KEY = "movieDataObjectKey";
    private final static String STATE_POSITION_KEY = "statePositionKey";

    int mScrollPosition;
    GridLayoutManager gridLayoutManager;


    private String completePosterURL;
    private MovieData movieDataObject;

    static int index;

    //private ArrayList<MovieData> movieData = new ArrayList<MovieData>();

    public MovieFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            Log.v(LOG_TAG, "reached inside savedInstanceState if");
            // Restore value of members from saved state
            //mCurrentScore = savedInstanceState.getInt(STATE_SCORE);
            completePosterURL = savedInstanceState.getString(COMPLETE_POSTER_PATH_KEY);
            /*originalTitle = savedInstanceState.getString(ORIGINAL_TITLE_KEY);*/
            movieDataObject = savedInstanceState.getParcelable(MOVIE_DATA_OBJECT_KEY);
            mScrollPosition = savedInstanceState.getInt(STATE_POSITION_KEY);

        }
    }



 /*   @Override
    public void onPause(){
        super.onPause();
        index = gridView.getFirstVisiblePosition();
        Log.v(LOG_TAG, "onPause(): index = " + index);
    }

    @Override
    public void onResume(){
        Log.v(LOG_TAG, "onResume(): index = " + index);
        gridView.setSelection(index);
        super.onResume();
    }*/



    @Override
    public void onSaveInstanceState(Bundle outState) {
       // mCurrentPosition = gridView.getFirstVisiblePosition();

        outState.putString(COMPLETE_POSTER_PATH_KEY, completePosterURL);
        outState.putParcelable(MOVIE_DATA_OBJECT_KEY, movieDataObject);

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);

        RecyclerView.LayoutManager layoutManager = mMoviesRecyclerView.getLayoutManager();

        if(layoutManager != null && layoutManager instanceof GridLayoutManager){
            mScrollPosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }

        Log.v(LOG_TAG, "onSaveInstanceState, mScrollPosition = " + mScrollPosition);
        outState.putInt(STATE_POSITION_KEY, mScrollPosition);

        //SavedState newState = new SavedState(superState);
        //outState.scrollPosition = mScrollPosition;
        //return newState;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);

        mMoviesRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_movies);
        mMoviesRecyclerView.setHasFixedSize(false);


        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            gridLayoutManager = new GridLayoutManager(getActivity(), 2);
            mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
        }
        else{
            gridLayoutManager = new GridLayoutManager(getActivity(), 3);
            mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
        }
        //GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        //mMoviesRecyclerView.setLayoutManager(gridLayoutManager);

        mMovieAdapter = new CustomMovieAdapter(this, new ArrayList<String>());
        mMoviesRecyclerView.setAdapter(mMovieAdapter);

        updateMovies();

        RecyclerView.LayoutManager layoutManager = mMoviesRecyclerView.getLayoutManager();
        if(layoutManager != null){
            Log.v(LOG_TAG, "reached inside gridLayoutManager if, mScrollPosition = " + mScrollPosition);

            int count = layoutManager.getChildCount();
            //if(mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < count){
                Log.v(LOG_TAG, "reached inside mScrollPosition if, mScrollPosition = " + mScrollPosition);
                layoutManager.smoothScrollToPosition(mMoviesRecyclerView, null, mScrollPosition);
            //}
        }


        return rootView;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public void updateMovies()
    {
        if(isOnline()) {
            fetchMoviesTask = new FetchMoviesTask(mMovieAdapter, getActivity());
            fetchMoviesTask.execute();
        }
        else {
            CharSequence text = "Unable to load, due to unavailability in network.";
            Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    public void onStart()
        {
        super.onStart();
        updateMovies();
    }


    @Override
    public void onListItemClick(int clickedItemIndex) {
        completePosterURL = mMovieAdapter.getItem(clickedItemIndex).toString();

        Intent openMovieDetails = new Intent(getActivity(), DetailActivity.class);
        movieDataObject = fetchMoviesTask.movieDataList.get(clickedItemIndex);
        openMovieDetails.putExtra(getString(R.string.string_poster_path), completePosterURL);
        openMovieDetails.putExtra("movie_data", movieDataObject);

        startActivity(openMovieDetails);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this.getContext())
        {
            Cursor mMovieData = null;

            @Override
            protected void onStartLoading() {
                if(mMovieData != null)
                {
                    deliverResult(mMovieData);
                }
                else {
                    forceLoad();
                }
            }


            @Override
            public Cursor loadInBackground() {

                try {
                    return getContext().getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, null, null, null, null);
                }
                catch (Exception e)
                {
                    Log.e(LOG_TAG, "Failed to load data asynchronously");
                    e.printStackTrace();
                    return null;
                }

            }

            @Override
            public void deliverResult(Cursor data) {
                mMovieData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}




