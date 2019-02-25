package com.example.coolestmovieapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.coolestmovieapp.R;
import com.example.coolestmovieapp.data.MovieResponse;
import com.example.coolestmovieapp.util.MovieUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

    private List<MovieResponse> mMovieList;
    List<MovieResponse> mFavMovieList = new ArrayList<>();
    private Context mContext;
    private OnMovieClicked movieClicked;
    private boolean isFromFavorites;

    public interface OnMovieClicked {
        void onMovieClicked(MovieResponse movieResponse, ImageView imageView, boolean isFavourite);
    }

    public CustomAdapter(List<MovieResponse> mMovieList, Context mContext, boolean isFavourite, OnMovieClicked onMovieClicked) {
        this.mMovieList = mMovieList;
        this.mContext = mContext;
        movieClicked = onMovieClicked;
        isFromFavorites = isFavourite;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = layoutInflater.inflate(R.layout.item_movie_view, viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CustomViewHolder customViewHolder, int i) {
        final MovieResponse movieResponse = mMovieList.get(i);
        customViewHolder.movieNameLabel.setText(mContext.getString(R.string.txt_movie_title, movieResponse.getTitle(), movieResponse.getYear()));
        customViewHolder.directorNameLabel.setText(mContext.getString(R.string.txt_director_name, movieResponse.getDirector()));
        customViewHolder.plotLabel.setText(mContext.getString(R.string.txt_plot, movieResponse.getPlot()));
        if (isFromFavorites)
            MovieUtil.toggleLikeImage(mContext, customViewHolder.likeImg, movieResponse);
        else
            customViewHolder.likeImg.setVisibility(View.GONE);
        Picasso.with(mContext).load(movieResponse.getPoster()).into(customViewHolder.moviePosterImg);
        customViewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                movieClicked.onMovieClicked(movieResponse, customViewHolder.moviePosterImg, isFromFavorites || MovieUtil.isMovieAlreadyFavorite(mMovieList, movieResponse, mContext));
            }
        });
        customViewHolder.likeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFromFavorites) {
                    MovieUtil.deleteFavouriteMovie(mContext, movieResponse);
                    customViewHolder.likeImg.setImageDrawable(mContext.getDrawable(R.drawable.ic_heart_unliked));
                    return;
                }
                if (MovieUtil.readFavouriteMovie(mContext) == null) {
                    mFavMovieList.add(movieResponse);
                    MovieUtil.writeFavoriteMovie(mContext, mFavMovieList);
                } else {
                    mFavMovieList = MovieUtil.readFavouriteMovie(mContext);
                    mFavMovieList.add(movieResponse);
                    MovieUtil.writeFavoriteMovie(mContext, mFavMovieList);
                }
                MovieUtil.toggleLikeImage(mContext, customViewHolder.likeImg, movieResponse);

            }
        });


    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        private ImageView moviePosterImg;
        private TextView movieNameLabel;
        private TextView directorNameLabel;
        private TextView plotLabel;
        private ImageView likeImg;
        private View parentLayout;

        CustomViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            moviePosterImg = mView.findViewById(R.id.PosterImg);
            movieNameLabel = mView.findViewById(R.id.MovieNameLabel);
            directorNameLabel = mView.findViewById(R.id.DirectorNameLabel);
            plotLabel = mView.findViewById(R.id.PlotLabel);
            likeImg = mView.findViewById(R.id.LikeImg);
            parentLayout = mView.findViewById(R.id.ParentLayout);

        }
    }
}
