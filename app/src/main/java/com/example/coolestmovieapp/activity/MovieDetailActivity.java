package com.example.coolestmovieapp.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.transition.Fade;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.coolestmovieapp.R;
import com.example.coolestmovieapp.constants.MovieConstants;
import com.example.coolestmovieapp.data.MovieResponse;
import com.example.coolestmovieapp.util.MovieUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieDetailActivity extends AppCompatActivity {


    private ImageView mPosterImg;
    private ImageView mLikeImg;
    private ImageView mWatchTrailerBtn;
    private TextView mMovieDetailLabel;
    private MovieResponse mMovieResponse;
    private boolean isFavourite;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Fade());
            getWindow().setExitTransition(new Fade());
        }
        setContentView(R.layout.activity_movie_detail);
        readExtra();
        initializeUiComponents();
        setData();
    }

    private void readExtra() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(MovieConstants.EXTRA_MOVIE)) {
            mMovieResponse = extras.getParcelable(MovieConstants.EXTRA_MOVIE);
        }
        if (extras != null && extras.containsKey(MovieConstants.EXTRA_FAVOURITE_MOVIE)) {
            isFavourite = extras.getBoolean(MovieConstants.EXTRA_FAVOURITE_MOVIE);
        }
    }

    private void initializeUiComponents() {
        mActionBar = getSupportActionBar();
        mPosterImg = findViewById(R.id.PosterImg);
        mWatchTrailerBtn = findViewById(R.id.WatchTrailerImg);
        mMovieDetailLabel = findViewById(R.id.MovieDetailLabel);
        mLikeImg = findViewById(R.id.LikeImg);
        mLikeImg.setVisibility((isFavourite) ? View.GONE : View.VISIBLE);
        mWatchTrailerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchYoutube();
            }
        });
        mLikeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<MovieResponse> favouriteMovie = new ArrayList<>();
                if (MovieUtil.readFavouriteMovie(MovieDetailActivity.this) == null) {
                    favouriteMovie.add(mMovieResponse);
                    MovieUtil.writeFavoriteMovie(MovieDetailActivity.this, favouriteMovie);
                } else {
                    favouriteMovie = MovieUtil.readFavouriteMovie(MovieDetailActivity.this);
                    favouriteMovie.add(mMovieResponse);
                    MovieUtil.writeFavoriteMovie(MovieDetailActivity.this, favouriteMovie);
                }
                MovieUtil.toggleLikeImage(MovieDetailActivity.this, mLikeImg, mMovieResponse);
                Intent intent = new Intent();
                intent.putExtra(MovieConstants.RESULT_MOVIE_LIKE, true);
                setResult(MovieConstants.REQUEST_CODE_MOVIE_LIKE, intent);

            }
        });
    }

    private void launchYoutube() {
        Intent intent = new Intent(Intent.ACTION_SEARCH);
        intent.setPackage("com.google.android.youtube");
        intent.putExtra("query", mMovieResponse.getTitle());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void setData() {
        if (mMovieResponse != null) {
            if (!TextUtils.isEmpty(mMovieResponse.getPoster())) {
                Picasso.with(this).load(mMovieResponse.getPoster()).into(mPosterImg);
            }
            String description = "";
            if (!TextUtils.isEmpty(mMovieResponse.getGenre()) && !TextUtils.isEmpty(mMovieResponse.getRated())) {
                description += getString(R.string.txt_genre_rating, mMovieResponse.getGenre(), mMovieResponse.getRated());
            }
            if (!TextUtils.isEmpty(mMovieResponse.getActors()) && !TextUtils.isEmpty(mMovieResponse.getLanguage())) {
                description += getString(R.string.txt_actors_awards, mMovieResponse.getActors(), mMovieResponse.getLanguage());
            }
            if (!TextUtils.isEmpty(mMovieResponse.getCountry()) && !TextUtils.isEmpty(mMovieResponse.getAwards())) {
                description += getString(R.string.txt_country_language, mMovieResponse.getCountry(), mMovieResponse.getAwards());
            }
            if (mMovieResponse.getRatings() != null && !mMovieResponse.getRatings().isEmpty()) {
                for (int i = 0; i < mMovieResponse.getRatings().size(); i++) {
                    description += getString(R.string.txt_ratings, mMovieResponse.getRatings().get(i).getSource(), mMovieResponse.getRatings().get(i).getValue());
                }
            }
            mMovieDetailLabel.setText(description);
            mActionBar.setTitle((mMovieResponse.getTitle() != null) ? mMovieResponse.getTitle() : getString(R.string.app_name));
            mActionBar.show();
        }
    }
}
