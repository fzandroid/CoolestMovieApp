package com.example.coolestmovieapp.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class MovieSearchItems implements Parcelable {

    private List<MovieResponse> movieItems;

    public MovieSearchItems(List<MovieResponse> movieItems) {
        this.movieItems = movieItems;
    }

    public List<MovieResponse> getMovieItems() {
        return movieItems;
    }

    public void setMovieItems(List<MovieResponse> movieItems) {
        this.movieItems = movieItems;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.movieItems);
    }

    protected MovieSearchItems(Parcel in) {
        this.movieItems = in.createTypedArrayList(MovieResponse.CREATOR);
    }

    public static final Parcelable.Creator<MovieSearchItems> CREATOR = new Parcelable.Creator<MovieSearchItems>() {
        @Override
        public MovieSearchItems createFromParcel(Parcel source) {
            return new MovieSearchItems(source);
        }

        @Override
        public MovieSearchItems[] newArray(int size) {
            return new MovieSearchItems[size];
        }
    };
}
