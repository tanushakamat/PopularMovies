package com.nanodegree.tkamat.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
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

    private static View rootView;

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

    private static int[] scrollPosition;

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

        //outState.putInt(SCROLL_POSITION_KEY, getScrollXY());
        outState.putIntArray("ARTICLE_SCROLL_POSITION",
                new int[]{rootView.getScrollX(), rootView.getScrollY()});

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_detail, container, false);

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

        //Log.v(LOG_TAG, "Reached here: DetailActivityFragment.onCreateView");

        String poster_path = getString(R.string.string_poster_path);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(poster_path)) {
            completePosterPath = intent.getStringExtra(poster_path);

            movieData = intent.getParcelableExtra("movie_data");
            if (movieData != null) {
                originalTitle = movieData.originalTitle;
                releaseDate = movieData.releaseDate;
                voteAverage = movieData.voteAverage;
                overview = movieData.overview;
                mMovieId = movieData.id;
            }

            //Log.v(LOG_TAG, "mMovieId in DAF = " + mMovieId);

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
        final Drawable favStarIcon = ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.favorite_icon);

        final Drawable favStarIconRed = ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.favorite_icon_red);
        favStarIconRed.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);

        toggleButton = (ToggleButton) rootView.findViewById(R.id.myToggleButton);
        if (MovieFragment.favouriteMovieIds.contains(mMovieId)) {
            toggleButton.setChecked(true);
            toggleButton.setBackgroundDrawable(favStarIconRed);

        } else {
            toggleButton.setChecked(false);
            toggleButton.setBackgroundDrawable(favStarIcon);
        }

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    favStarIconRed.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                    toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.favorite_icon_red));
                    addToFavourites(mMovieId);
                } else {
                    toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.favorite_icon));
                    deleteFromFavourites(mMovieId);
                }
            }
        });

        if(savedInstanceState!=null) {
            /*final int[] */scrollPosition = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION");
        }

        return rootView;
    }

    public static void  restoreScrollPosition()
    {
        if (scrollPosition != null && rootView != null)
            rootView.post(new Runnable() {
                public void run() {
                    //Log.v(LOG_TAG, "Reaching near scrollto, position[0] = " + scrollPosition[0] + " position[1] = " + scrollPosition[1]);
                    rootView.scrollTo(scrollPosition[0], scrollPosition[1]);
                }
            });
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    void updateTrailers() {
        if (isOnline()) {
            fetchTrailersTask = new FetchTrailersTask(mTrailerAdapter, getActivity());
            fetchTrailersTask.execute(mMovieId);
        } else {
            CharSequence text = "Unable to load, due to unavailability in network.";
            Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    void updateReviews() {
        if (isOnline()) {
            fetchReviewsTask = new FetchReviewsTask(mReviewAdapter, getActivity());
            fetchReviewsTask.execute(mMovieId);
        } else {
            CharSequence text = "Unable to load, due to unavailability in network.";
            Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }

    }

    void addToFavourites(String movieId) {
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_MOVIEID, movieId);

        Uri uri = getContext().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, values);
    }

    void deleteFromFavourites(String movieId) {
        Uri deleteUri = MovieContract.MovieEntry.CONTENT_URI;
        deleteUri = deleteUri.buildUpon().appendPath(movieId).build();
        int rowsDeleted = getContext().getContentResolver().delete(deleteUri, null, null);

        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_MOVIEID, movieId);
        //Toast.makeText(getContext(), rowsDeleted + " " + deleteUri.toString() , Toast.LENGTH_LONG).show();

    }


    public void onStart() {
        super.onStart();
        updateTrailers();
        updateReviews();
    }

    public void onResume() {
        super.onResume();
        updateTrailers();
        updateReviews();
    }


    @Override
    public void onListItemClick(int clickedItemIndex, String trailerYoutubeLink) {
        //Log.v(LOG_TAG, "Reached in DetailActivityFragment.onListItemClick");

        Uri uri = Uri.parse(trailerYoutubeLink);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onReviewListItemClick(int clickedItemIndex, String trailerYoutubeLink) {
        //Toast.makeText(getContext(), "Clicked Review", Toast.LENGTH_SHORT).show();
    }
}
