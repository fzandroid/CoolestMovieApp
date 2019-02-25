package com.example.coolestmovieapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.coolestmovieapp.R;
import com.example.coolestmovieapp.constants.MovieConstants;
import com.example.coolestmovieapp.data.MovieResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class MovieUtil {
    private static final String url = "http://www.omdbapi.com/";
    private static final String api_key = "96b1078a";
    public static final String KEY_MOVIE_FAVOURITE = "movieFavourite";

    public static String createURLEndpoint(String movieName, boolean imdbId) {
        if (imdbId) {
            return url + "?apikey=" + api_key + "&i=" + movieName;
        } else
            return url + "?apikey=" + api_key + "&s=" + movieName;
    }

    synchronized public static void writeFavoriteMovie(Context context, List<MovieResponse> favoriteList) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(favoriteList);
        editor.putString(KEY_MOVIE_FAVOURITE, json);
        editor.commit();
        showToast(context, context.getString(R.string.txt_movie_saved));
    }

    synchronized public static List<MovieResponse> readFavouriteMovie(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(KEY_MOVIE_FAVOURITE, "");
        Type type = new TypeToken<List<MovieResponse>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    synchronized public static void deleteFavouriteMovie(Context context, MovieResponse response) {
        List<MovieResponse> movieResponses = readFavouriteMovie(context);
        MovieResponse movieResponse = null;
        for (MovieResponse toBeDeletedMovie : movieResponses) {
            if (toBeDeletedMovie.getImdbId().equalsIgnoreCase(response.getImdbId())) {
                movieResponse = toBeDeletedMovie;
            }

        }
        movieResponses.remove(movieResponse);
        writeFavoriteMovie(context, movieResponses);
        showToast(context, context.getString(R.string.txt_movie_removed));

    }


    public static void toggleLikeImage(Context context, ImageView imageView, MovieResponse response) {
        if (response.isFavourite()) {
            imageView.setImageDrawable(context.getDrawable(R.drawable.ic_heart_unliked));
            deleteFavouriteMovie(context, response);
            imageView.setTag(MovieConstants.LIKE_IMG_TAG);
        } else {
            imageView.setImageDrawable(context.getDrawable(R.drawable.ic_heart_liked));
            imageView.setTag(context.getClass().getName());
            imageView.setTag(null);

        }
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message,
                Toast.LENGTH_LONG).show();

    }

    public static void dismissKeyBoard(EditText editText, Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static boolean isMovieAlreadyFavorite(List<MovieResponse> movieList, MovieResponse response, Context context) {
        for (MovieResponse response1 : readFavouriteMovie(context)) {
            if (response1.getImdbId().equalsIgnoreCase(response.getImdbId())) {
                return true;
            }
        }
        return false;
    }


}
