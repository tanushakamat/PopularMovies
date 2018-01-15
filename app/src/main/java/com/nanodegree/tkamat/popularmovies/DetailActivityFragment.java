package com.nanodegree.tkamat.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.nanodegree.tkamat.popularmovies.data.MovieContract;
import com.nanodegree.tkamat.popularmovies.reviews.FetchReviewsTask;
import com.nanodegree.tkamat.popularmovies.reviews.ReviewsAdapter;
import com.nanodegree.tkamat.popularmovies.trailers.FetchTrailersTask;
import com.nanodegree.tkamat.popularmovies.trailers.TrailersAdapter;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements TrailersAdapter.ListItemClickListener, ReviewsAdapter.ListItemClickListener {

    private FetchTrailersTask fetchTrailersTask;
    private FetchReviewsTask fetchReviewsTask;

    String completePosterPath;
    String originalTitle;
    String releaseDate;
    String voteAverage;
    String overview;

    MovieData movieData;

    RecyclerView mTrailersRecyclerView;
    RecyclerView mReviewsRecyclerView;
    TrailersAdapter mTrailerAdapter;
    ReviewsAdapter mReviewAdapter;

    private final static String COMPLETE_POSTER_PATH_KEY = "completePosterPathKey";
    private final static String ORIGINAL_TITLE_KEY = "originalTitleKey";
    private final static String RELEASE_DATE_KEY = "releaseDateKey";
    private final static String VOTE_AVERAGE_KEY = "voteAverageKey";
    private final static String OVERVIEW_KEY = "overviewKey";

    public String mMovieId;
    final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            //mCurrentScore = savedInstanceState.getInt(STATE_SCORE);
            //mCurrentLevel = savedInstanceState.getInt(STATE_LEVEL);
            completePosterPath = savedInstanceState.getString(COMPLETE_POSTER_PATH_KEY);
            originalTitle = savedInstanceState.getString(ORIGINAL_TITLE_KEY);
            releaseDate = savedInstanceState.getString(RELEASE_DATE_KEY);
            voteAverage = savedInstanceState.getString(VOTE_AVERAGE_KEY);
            overview = savedInstanceState.getString(OVERVIEW_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(COMPLETE_POSTER_PATH_KEY, completePosterPath);
        outState.putString(ORIGINAL_TITLE_KEY, originalTitle);
        outState.putString(RELEASE_DATE_KEY, releaseDate);
        outState.putString(VOTE_AVERAGE_KEY, voteAverage);
        outState.putString(OVERVIEW_KEY, overview);

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    /*
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);


        // Restore state members from saved instance
        mCurrentScore = savedInstanceState.getInt(STATE_SCORE);
        mCurrentLevel = savedInstanceState.getInt(STATE_LEVEL);
    }*/



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_detail, container, false);

        mTrailersRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_trailers);
        mTrailersRecyclerView.setHasFixedSize(true);

        LinearLayoutManager trailersLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mTrailersRecyclerView.setLayoutManager(trailersLayoutManager);

        mTrailerAdapter = new TrailersAdapter(this);
        mTrailersRecyclerView.setAdapter(mTrailerAdapter);

        DividerItemDecoration mTrailerDividerItemDecoration = new DividerItemDecoration(
                mTrailersRecyclerView.getContext(),
                trailersLayoutManager.getOrientation()
        );
        mTrailersRecyclerView.addItemDecoration(mTrailerDividerItemDecoration);

        mReviewsRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_reviews);
        mReviewsRecyclerView.setHasFixedSize(true);

        LinearLayoutManager reviewsLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mReviewsRecyclerView.setLayoutManager(reviewsLayoutManager);

        mReviewAdapter = new ReviewsAdapter(this);
        mReviewsRecyclerView.setAdapter(mReviewAdapter);

        DividerItemDecoration mReviewDividerItemDecoration = new DividerItemDecoration(
                mReviewsRecyclerView.getContext(),
                reviewsLayoutManager.getOrientation()
        );
        mReviewsRecyclerView.addItemDecoration(mReviewDividerItemDecoration);

        Log.v(LOG_TAG, "Reached here: DetailActivityFragment.onCreateView");

        String poster_path = getString(R.string.string_poster_path);
        Intent intent = getActivity().getIntent();
        if(intent!=null && intent.hasExtra(poster_path))
        {
            completePosterPath = intent.getStringExtra(poster_path);
            /*originalTitle = intent.getStringExtra(getString(R.string.string_original_title));
            releaseDate = intent.getStringExtra(getString(R.string.string_release_date));
            voteAverage = intent.getStringExtra(getString(R.string.string_vote_average));
            overview = intent.getStringExtra(getString(R.string.string_overview));
            mMovieId = intent.getStringExtra("id");*/

            movieData = intent.getParcelableExtra("movie_data");
            if(movieData!=null)
            {
                originalTitle = movieData.originalTitle;
                releaseDate = movieData.releaseDate;
                voteAverage = movieData.voteAverage;
                overview = movieData.overview;
                mMovieId = movieData.id;
            }

            Log.v(LOG_TAG, "mMovieId in DAF = " + mMovieId);

            updateTrailers();
            updateReviews();


                    ImageView imageView = (ImageView) rootView.findViewById(R.id.poster_thumbnail);
            Picasso.with(getActivity()).load(completePosterPath).into((ImageView) imageView);

            TextView textView_overview = (TextView) rootView.findViewById(R.id.movie_overview);
            textView_overview.setText(overview);
            textView_overview.setMovementMethod(new ScrollingMovementMethod());

            TextView textView_originalTitle = (TextView) rootView.findViewById(R.id.movie_title);
            textView_originalTitle.setText(originalTitle);

            TextView textView_voteAverage = (TextView) rootView.findViewById(R.id.movie_rating);
            textView_voteAverage.setText(voteAverage);

            TextView textView_releaseDate = (TextView) rootView.findViewById(R.id.movie_releasedate);
            textView_releaseDate.setText(releaseDate);
        }

        final ToggleButton toggleButton;
        toggleButton = (ToggleButton) rootView.findViewById(R.id.myToggleButton);
        toggleButton.setChecked(false);
        toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.favorite_icon));
        final Drawable favStarIconRed = getActivity().getApplicationContext().getResources().getDrawable(R.drawable.favorite_icon_red);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.favorite_icon));
                    //buttonView.setBackgroundColor(Color.GREEN);
                    addToFavourites(mMovieId);
                }
                else {
                    favStarIconRed.setColorFilter(0xffff0000, PorterDuff.Mode.SRC_ATOP);
                    toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.favorite_icon_red));
                    deleteFromFavourites(mMovieId);
                }
            }
        });

        return rootView;
    }

    void updateTrailers()
    {
        fetchTrailersTask = new FetchTrailersTask(mTrailerAdapter, getActivity());
        fetchTrailersTask.execute(mMovieId);
    }

    void updateReviews()
    {
        fetchReviewsTask = new FetchReviewsTask(mReviewAdapter, getActivity());
        fetchReviewsTask.execute(mMovieId);
    }

    void addToFavourites(String movieId)
    {
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_MOVIEID, movieId);

        Uri uri = getContext().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, values);

        if(uri!=null)
        {
            Toast.makeText(getContext(), uri.toString(), Toast.LENGTH_LONG).show();
        }
        //getActivity().finish();
    }

    void deleteFromFavourites(String movieId)
    {
        Uri deleteUri = MovieContract.MovieEntry.CONTENT_URI;
        deleteUri.buildUpon().appendPath(movieId).build();
        int rowsDeleted = getContext().getContentResolver().delete(deleteUri, null, null);

        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_MOVIEID, movieId);

        Toast.makeText(getContext(), rowsDeleted + " " + deleteUri.toString() , Toast.LENGTH_LONG).show();
        //getActivity().finish();
    }


    public void onStart()
    {
        super.onStart();
    //    if(mMovieId!=null) {
            updateTrailers();
            updateReviews();
      //  }
    }

    public void onResume()
    {
        super.onResume();
        updateTrailers();
        updateReviews();
    }


    @Override
    public void onListItemClick(int clickedItemIndex, String trailerYoutubeLink) {
        Log.v(LOG_TAG, "Reached in DetailActivityFragment.onListItemClick");

        //try {
            Uri uri = Uri.parse(trailerYoutubeLink);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivity(intent);
            }
        /*} catch (Exception e)
        {
            e.printStackTrace();
        }*/


    }

    @Override
    public void onReviewListItemClick(int clickedItemIndex, String trailerYoutubeLink) {
        //Toast.makeText(getContext(), "Clicked Review", Toast.LENGTH_SHORT).show();
    }
}
