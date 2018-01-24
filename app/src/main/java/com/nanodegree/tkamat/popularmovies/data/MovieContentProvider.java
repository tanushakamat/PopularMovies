package com.nanodegree.tkamat.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.nanodegree.tkamat.popularmovies.data.MovieContract.CONTENT_AUTHORITY;
import static com.nanodegree.tkamat.popularmovies.data.MovieContract.MovieEntry.CONTENT_URI;
import static com.nanodegree.tkamat.popularmovies.data.MovieContract.MovieEntry.TABLE_NAME;
import static com.nanodegree.tkamat.popularmovies.data.MovieContract.PATH_FAVOURITES;

/**
 * Created by tnadkarn on 11/23/2017.
 */

public class MovieContentProvider extends ContentProvider {

    private MovieDbHelper mMovieDbHelper;

    private static final int FAVOURITES = 100;
    private static final int FAVOURITES_WITH_ID = 101;
    private static final int FAVOURITES_WITH_MOVIEID = 102;

    public final static UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_FAVOURITES, FAVOURITES);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_FAVOURITES + "/#", FAVOURITES_WITH_ID);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_FAVOURITES + "/*", FAVOURITES_WITH_MOVIEID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {

        Context context = getContext();
        mMovieDbHelper = new MovieDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = mMovieDbHelper.getReadableDatabase();

        int uriMatchReturn = sUriMatcher.match(uri);

        Cursor retCursor;

        switch (uriMatchReturn){
            case FAVOURITES:
                retCursor = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported Uri: " + uri);

        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder, @Nullable CancellationSignal cancellationSignal) {
        return super.query(uri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        Uri returnUri;

        int uriMatcherReturn = sUriMatcher.match(uri);

        switch (uriMatcherReturn) {
            case FAVOURITES:
                long id = db.insert(TABLE_NAME, null, contentValues);
                if(id>0)
                {
                    returnUri = ContentUris.withAppendedId(CONTENT_URI, id);
                }
                else
                {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Row cannot be inserted");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        int rowsDeleted;

        int uriMatcherReturn = sUriMatcher.match(uri);

        switch (uriMatcherReturn) {
            case FAVOURITES_WITH_ID:

                String id = uri.getPathSegments().get(1);
                String mSelection = "movieid=?";
                String[] mSelectionArgs = new String[]{id};


                rowsDeleted = db.delete(TABLE_NAME, mSelection, mSelectionArgs);
                //return rowsDeleted;

                break;

            default:
                throw new UnsupportedOperationException("Not yet implemeted");
        }
        if(rowsDeleted!=0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
