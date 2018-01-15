package com.nanodegree.tkamat.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by tnadkarn on 11/23/2017.
 */

public class MovieContract {

    public final static String CONTENT_AUTHORITY = "com.nanodegree.tkamat.popularmovies";
    public final static Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FAVOURITES = "favouritemovies";

    public final static class MovieEntry implements BaseColumns{
        //content://com.nanodegree.tkamat.popularmovies/favouritemovies
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVOURITES).build();

        public static final String TABLE_NAME = "favouritemovies";
        public static final String COLUMN_MOVIEID = "movieid";
    }
}
