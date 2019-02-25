package com.example.coolestmovieapp.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Ratings implements Parcelable {

    private String source;
    private String value;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Ratings() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.source);
        dest.writeString(this.value);
    }

    public Ratings(Parcel in) {
        this.source = in.readString();
        this.value = in.readString();
    }

    public static final Parcelable.Creator<Ratings> CREATOR = new Parcelable.Creator<Ratings>() {
        @Override
        public Ratings createFromParcel(Parcel source) {
            return new Ratings(source);
        }

        @Override
        public Ratings[] newArray(int size) {
            return new Ratings[size];
        }
    };
}
