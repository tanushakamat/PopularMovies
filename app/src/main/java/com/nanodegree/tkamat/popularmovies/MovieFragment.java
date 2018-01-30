package com.nanodegree.tkamat.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
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
public class MovieFragment extends Fragment implements CustomMovieAdapter.ListItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    final String LOG_TAG = MovieFragment.class.getSimpleName();

    private CustomMovieAdapter mMovieAdapter;
    private FetchMoviesTask fetchMoviesTask;
    private static RecyclerView mMoviesRecyclerView;


    private final static String COMPLETE_POSTER_PATH_KEY = "completePosterPathKey";
    private final static String MOVIE_DATA_OBJECT_KEY = "movieDataObjectKey";
    private final static String STATE_POSITION_KEY = "statePositionKey";
    private final static String FAVOURITE_MOVIE_IDS_KEY = "favouriteMovieIdsKey";
    private final static String SAVED_LAYOUT_MANAGER = "savedLayoutManager";

    static int mScrollPosition;
    GridLayoutManager gridLayoutManager;

    public static Parcelable layoutManagerSavedState;


    private String completePosterURL;
    private MovieData movieDataObject;


    private static final int CURSOR_LOADER_ID = 0;
    private Cursor mCursor;

    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    public static ArrayList<String> favouriteMovieIds = new ArrayList<String>();


    public MovieFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState != null) {
            Log.v(LOG_TAG, "reached inside savedInstanceState if");
            // Restore value of members from saved state
            completePosterURL = savedInstanceState.getString(COMPLETE_POSTER_PATH_KEY);
            movieDataObject = savedInstanceState.getParcelable(MOVIE_DATA_OBJECT_KEY);
            mScrollPosition = savedInstanceState.getInt(STATE_POSITION_KEY);
            favouriteMovieIds = savedInstanceState.getStringArrayList(FAVOURITE_MOVIE_IDS_KEY);

            layoutManagerSavedState = ((Bundle) savedInstanceState).getParcelable(SAVED_LAYOUT_MANAGER);

        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(COMPLETE_POSTER_PATH_KEY, completePosterURL);
        outState.putParcelable(MOVIE_DATA_OBJECT_KEY, movieDataObject);
        outState.putStringArrayList(FAVOURITE_MOVIE_IDS_KEY, favouriteMovieIds);

        outState.putParcelable(SAVED_LAYOUT_MANAGER, mMoviesRecyclerView.getLayoutManager().onSaveInstanceState());

        // call superclass to save any view hierarchy
 /*       super.onSaveInstanceState(outState);

        RecyclerView.LayoutManager layoutManager = mMoviesRecyclerView.getLayoutManager();

        if (layoutManager != null && layoutManager instanceof GridLayoutManager) {
            mScrollPosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }

        //Log.v(LOG_TAG, "onSaveInstanceState, mScrollPosition = " + mScrollPosition);
        outState.putInt(STATE_POSITION_KEY, mScrollPosition);*/
    }


    @Override
    public void onStart() {
        super.onStart();

        //listener on changed sort order preference:
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
//                if (key.equals(getActivity().getString(R.string.key_sort_order))) {
                //Log.v(LOG_TAG, "Settings key changed: " + key);
                updateMoviesOnPreferenceChange(key);
                //              }

            }
        };
        prefs.registerOnSharedPreferenceChangeListener(prefListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.v(LOG_TAG, "Inside Oncreateview");

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mMoviesRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_movies);


        mMoviesRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_movies);
        mMoviesRecyclerView.setHasFixedSize(false);


        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager = new GridLayoutManager(getActivity(), 2);
            mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
        } else {
            gridLayoutManager = new GridLayoutManager(getActivity(), 3);
            mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
        }

        mMovieAdapter = new CustomMovieAdapter(this, new ArrayList<String>());
        mMoviesRecyclerView.setAdapter(mMovieAdapter);

 /*       mMoviesRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                restoreRecyclerViewState();
            }
        });*/

        String sortingOrder = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getContext().getString(R.string.key_sort_order), "popular");
        if (sortingOrder.equals("popular") || sortingOrder.equals("top_rated")) {
            updateMovies();
        } else {
            Bundle args = this.getArguments();
            getLoaderManager().initLoader(CURSOR_LOADER_ID, args, this);
        }
        return rootView;
    }


    public static void restoreRecyclerViewState() {
        if (layoutManagerSavedState != null) {
            mMoviesRecyclerView.getLayoutManager().onRestoreInstanceState(layoutManagerSavedState);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public void updateMoviesOnPreferenceChange(String key) {
        try {
            String sortingOrder = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(key, "popular");
            if (sortingOrder.equals("popular") || sortingOrder.equals("top_rated")) {
                updateMovies();
            } else {
                Bundle args = this.getArguments();
                getLoaderManager().restartLoader(CURSOR_LOADER_ID, args, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateMovies() {
        if (isOnline()) {
            fetchMoviesTask = new FetchMoviesTask(mMovieAdapter, getActivity());
            fetchMoviesTask.execute();
        } else {
            CharSequence text = "Unable to load, due to unavailability in network.";
            Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
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
        Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;

        return new CursorLoader(getActivity(),
                movieUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        String movieID;
        mCursor = data;
        Log.v(LOG_TAG, "mCursor.getCount() = " + mCursor.getCount());
        mCursor.moveToFirst();
        favouriteMovieIds.clear();
        while (!mCursor.isAfterLast()) {
            movieID = mCursor.getString(1);
            Log.v(LOG_TAG, movieID);
            favouriteMovieIds.add(movieID);
            mCursor.moveToNext();
        }
        updateMovies();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
    }

}




