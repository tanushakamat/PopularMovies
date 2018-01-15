package com.nanodegree.tkamat.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nanodegree.tkamat.popularmovies.data.MovieContract.MovieEntry;

/**
 * Created by tnadkarn on 11/23/2017.
 */

public class MovieDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movieDB.db";
    private static final int VERSION = 1;

    MovieDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String CREATE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + "(" +
                            MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                            MovieEntry.COLUMN_MOVIEID + " TEXT NOT NULL); ";
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
