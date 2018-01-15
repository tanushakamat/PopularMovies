package com.nanodegree.tkamat.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tnadkarn on 3/9/2017.
 */

public class MovieData implements Parcelable{
    public String originalTitle;
    public String releaseDate;
    public String voteAverage;
    public String overview;
    public String id;

    MovieData(String originalTitle, String releaseDate, String voteAverage, String overview, String id)
    {
        this.originalTitle = originalTitle;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.overview = overview;
        this.id = id;
    }

    public static final Parcelable.Creator<MovieData> CREATOR = new Parcelable.Creator<MovieData>(){

        @Override
        public MovieData createFromParcel(Parcel parcel) {
            return new MovieData(parcel);
        }

        @Override
        public MovieData[] newArray(int i) {
            return new MovieData[i];
        }
    };

    MovieData(Parcel in){
        this.originalTitle = in.readString();
        this.releaseDate = in.readString();
        this.voteAverage = in.readString();
        this.overview = in.readString();
        this.id = in.readString();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(originalTitle);
        parcel.writeString(releaseDate);
        parcel.writeString(voteAverage);
        parcel.writeString(overview);
        parcel.writeString(id);
    }
}
