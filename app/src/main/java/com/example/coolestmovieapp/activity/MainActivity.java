package com.example.coolestmovieapp.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.coolestmovieapp.ItemDecorater.MovieListItemDecorater;
import com.example.coolestmovieapp.R;
import com.example.coolestmovieapp.adapter.CustomAdapter;
import com.example.coolestmovieapp.constants.MovieConstants;
import com.example.coolestmovieapp.constants.OperationsCodes;
import com.example.coolestmovieapp.data.MovieResponse;
import com.example.coolestmovieapp.data.Ratings;
import com.example.coolestmovieapp.network.AppController;
import com.example.coolestmovieapp.util.MovieUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private MovieResponse mMovieResponse;
    private EditText mSearchBox;
    private View mSearchBtn;
    private RecyclerView mMovieList;
    private CustomAdapter mAdapter;
    private List<MovieResponse> movieList;
    private ProgressBar mProgressBar;
    private Menu mLikedMovies;
    private boolean isFavouriteView;
    private int numberOfCalls = 0;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUi();
        setClickListeners();
        showFavoriteMovies();


    }

    private void initializeUi() {
        mSearchBox = findViewById(R.id.SearchBox);
        mSearchBtn = findViewById(R.id.SearchBtn);
        mMovieList = findViewById(R.id.MovieSearchList);
        mProgressBar = findViewById(R.id.MovieProgressBar);
    }

    private void setClickListeners() {
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mSearchBox.getText().toString())) {
                    MovieUtil.dismissKeyBoard(mSearchBox, MainActivity.this);
                    dispatchRequest(OperationsCodes.MAKE_SEARCH_MOVIE_CALL, mSearchBox.getText().toString(), false);
                    movieList = new ArrayList<>();
                    movieList.clear();
                }
                mLikedMovies.getItem(0).setIcon(getDrawable(R.drawable.ic_heart_unliked));
            }
        });

    }

    private void showFavoriteMovies() {
        if (MovieUtil.readFavouriteMovie(MainActivity.this) == null || MovieUtil.readFavouriteMovie(MainActivity.this).size() == 0) {
            MovieUtil.showToast(MainActivity.this, getString(R.string.txt_no_favourite));
            mProgressBar.setVisibility(View.GONE);

        } else {
            setResponseList(MovieUtil.readFavouriteMovie(MainActivity.this), true);
        }

    }

    private void dispatchRequest(final String operationcode, String name, boolean imdbID) {
        mProgressBar.setVisibility(View.VISIBLE);
        JsonObjectRequest movieListRequest = new JsonObjectRequest(Request.Method.GET, MovieUtil.createURLEndpoint(name, imdbID), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response.has(MovieConstants.KEY_ERROR)) {
                    String error = response.optString(MovieConstants.KEY_ERROR);
                    if (TextUtils.equals(error, MovieConstants.MOVIE_NOT_FOUND)) {
                        MovieUtil.showToast(MainActivity.this, getString(R.string.txt_network_error));
                        return;
                    }
                }

                try {

                    if (operationcode.equalsIgnoreCase(OperationsCodes.MAKE_SEARCH_MOVIE_CALL)) {
                        JSONArray listResponse = response.getJSONArray(MovieConstants.KEY_SEARCH);
                        for (int i = 0; i < listResponse.length(); i++) {
                            setData(listResponse.getJSONObject(i), true, movieList);
                        }
                        for (MovieResponse movieDetail : movieList) {
                            dispatchDetailCall(movieDetail.getImdbId());
                        }
                    } else if (operationcode.equalsIgnoreCase(OperationsCodes.MAKE_DEATIL_MOVIE_CALL)) {
                        setData(response, false, movieList);
                        for (MovieResponse movieDetail : movieList) {
                            if (TextUtils.equals(movieDetail.getImdbId(), mMovieResponse.getImdbId())) {
                                movieList.set(movieList.indexOf(movieDetail), mMovieResponse);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                counter++;
                if (counter == numberOfCalls) {
                    setResponseList(movieList, false);
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(MovieConstants.TAG_VOLLEY, error.toString());
                MovieUtil.showToast(MainActivity.this, getString(R.string.txt_network_error));

            }
        });
        AppController.getInstance().addToRequestQueue(movieListRequest);

    }

    private void setResponseList(List<MovieResponse> movieList, boolean hasLiked) {
        mProgressBar.setVisibility(View.GONE);
        mAdapter = new CustomAdapter(movieList, getApplicationContext(), hasLiked, new CustomAdapter.OnMovieClicked() {
            @Override
            public void onMovieClicked(MovieResponse movieResponse, ImageView imageView, boolean isFavourite) {
                Intent intent = new Intent(MainActivity.this, MovieDetailActivity.class);
                intent.putExtra(MovieConstants.EXTRA_MOVIE, movieResponse);
                intent.putExtra(MovieConstants.EXTRA_FAVOURITE_MOVIE, isFavourite);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, imageView, getString(R.string.transition_image));
                    startActivityForResult(intent, MovieConstants.REQUEST_CODE_MOVIE_LIKE, activityOptions.toBundle());
                } else {
                    startActivityForResult(intent, MovieConstants.REQUEST_CODE_MOVIE_LIKE);
                }

            }
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        mMovieList.setLayoutManager(layoutManager);
        mMovieList.setAdapter(mAdapter);
        setMovieListItemDecorater();
    }

    private void setMovieListItemDecorater() {
        mMovieList.addItemDecoration(new MovieListItemDecorater(ContextCompat.getDrawable(MainActivity.this, R.drawable.item_seperator)));
    }

    private void dispatchDetailCall(String id) {
        dispatchRequest(OperationsCodes.MAKE_DEATIL_MOVIE_CALL, id, true);
        numberOfCalls++;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mLikedMovies = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mLikedMovies.getItem(0).setIcon(getDrawable(R.drawable.ic_heart_liked));
        showFavoriteMovies();
        return super.onOptionsItemSelected(item);
    }

    private void setData(JSONObject movieResponseObject, boolean addToList, List<MovieResponse> movieList) {
        mMovieResponse = new MovieResponse();
        try {
            if (movieResponseObject.has(MovieConstants.TITLE_KEY))
                mMovieResponse.setTitle(movieResponseObject.getString(MovieConstants.TITLE_KEY));
            if (movieResponseObject.has(MovieConstants.IMDB_ID_KEY))
                mMovieResponse.setImdbId(movieResponseObject.getString(MovieConstants.IMDB_ID_KEY));
            if (movieResponseObject.has(MovieConstants.YEAR_KEY))
                mMovieResponse.setYear(movieResponseObject.getString(MovieConstants.YEAR_KEY));
            if (movieResponseObject.has(MovieConstants.RATED_KEY))
                mMovieResponse.setRated(movieResponseObject.getString(MovieConstants.RATED_KEY));
            if (movieResponseObject.has(MovieConstants.RELEASED_KEY))
                mMovieResponse.setReleased(movieResponseObject.getString(MovieConstants.RELEASED_KEY));
            if (movieResponseObject.has(MovieConstants.GENRE_KEY))
                mMovieResponse.setGenre(movieResponseObject.getString(MovieConstants.GENRE_KEY));
            if (movieResponseObject.has(MovieConstants.DIRECTOR_KEY))
                mMovieResponse.setDirector(movieResponseObject.getString(MovieConstants.DIRECTOR_KEY));
            if (movieResponseObject.has(MovieConstants.WRITER_KEY))
                mMovieResponse.setWriter(movieResponseObject.getString(MovieConstants.WRITER_KEY));
            if (movieResponseObject.has(MovieConstants.ACTORS_KEY))
                mMovieResponse.setActors(movieResponseObject.getString(MovieConstants.ACTORS_KEY));
            if (movieResponseObject.has(MovieConstants.PLOT_KEY))
                mMovieResponse.setPlot(movieResponseObject.getString(MovieConstants.PLOT_KEY));
            if (movieResponseObject.has(MovieConstants.LANGUAGE_KEY))
                mMovieResponse.setLanguage(movieResponseObject.getString(MovieConstants.LANGUAGE_KEY));
            if (movieResponseObject.has(MovieConstants.COUNTRY_KEY))
                mMovieResponse.setCountry(movieResponseObject.getString(MovieConstants.COUNTRY_KEY));
            if (movieResponseObject.has(MovieConstants.AWARDS_KEY))
                mMovieResponse.setAwards(movieResponseObject.getString(MovieConstants.AWARDS_KEY));
            if (movieResponseObject.has(MovieConstants.POSTERS_KEY))
                mMovieResponse.setPoster(movieResponseObject.getString(MovieConstants.POSTERS_KEY));
            if (movieResponseObject.has(MovieConstants.RATINGS_KEY)) {
                JSONArray ratingList = movieResponseObject.getJSONArray(MovieConstants.RATINGS_KEY);
                List<Ratings> ratings = new ArrayList<>();
                for (int j = 0; j < ratingList.length(); j++) {
                    JSONObject ratingsObject = ratingList.getJSONObject(j);
                    Ratings ratings1 = new Ratings();
                    if (ratingsObject.has(MovieConstants.SOURCE_KEY))
                        ratings1.setSource(ratingsObject.getString(MovieConstants.SOURCE_KEY));
                    if (ratingsObject.has(MovieConstants.VALUE_KEY))
                        ratings1.setValue(ratingsObject.getString(MovieConstants.VALUE_KEY));
                    ratings.add(ratings1);

                }
                mMovieResponse.setRatings(ratings);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (addToList) {
            movieList.add(mMovieResponse);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (requestCode == MovieConstants.REQUEST_CODE_MOVIE_LIKE) {
            if (data.getExtras() != null) {
                Bundle bundle = data.getExtras();
                if (bundle != null && bundle.containsKey(MovieConstants.RESULT_MOVIE_LIKE)) {
                    isFavouriteView = bundle.getBoolean(MovieConstants.RESULT_MOVIE_LIKE);
                    showFavoriteMovies();
                    mLikedMovies.getItem(0).setIcon(getDrawable(R.drawable.ic_heart_liked));
                }
            }
        }
    }
}

