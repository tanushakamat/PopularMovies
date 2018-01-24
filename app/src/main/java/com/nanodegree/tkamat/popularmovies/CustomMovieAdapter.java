package com.nanodegree.tkamat.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by tnadkarn on 1/19/2017.
 */

public class CustomMovieAdapter extends RecyclerView.Adapter<CustomMovieAdapter.MoviesViewHolder> {
    //private final Activity context;
    Context context;
    private static ListItemClickListener mOnClickListener;
    final String POSTERSIZE = "w185";
    final String IMAGEBASEURL = "http://image.tmdb.org/t/p/";

    private final String LOG_TAG = CustomMovieAdapter.class.getSimpleName();

    ArrayList<String> data;

    public CustomMovieAdapter(CustomMovieAdapter.ListItemClickListener listItemClickListener, ArrayList<String> arrayList) {
        this.mOnClickListener = listItemClickListener;
        this.data = arrayList;
    }

    @Override
    public MoviesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Log.v(LOG_TAG, "reached here CustomMovieAdapter.onCreateViewHolder");

        context = parent.getContext();
        int layoutIdForListItem = R.layout.grid_item_movies;
        boolean shouldAttachToParentImmediately = false;

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        MoviesViewHolder moviesViewHolder = new MoviesViewHolder(view);

        return moviesViewHolder;
    }

    @Override
    public void onBindViewHolder(MoviesViewHolder moviesViewHolder, int position) {

        //Log.v(LOG_TAG, "reached here CustomMovieAdapter.onBindViewHolder, position = " + position + "data.get(position) = " + data.get(position));

        try {
            Picasso
                    .with(context)
                    .load(data.get(position))
                    .into((ImageView) moviesViewHolder.imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        if (data == null) return 0;
        else
            return data.size();
    }


    public String getItem(int position) {
        return data.get(position);
    }

    public void clear() {
        if (data != null) {
            Log.v(LOG_TAG, "Reached data.clear()");
            data.clear();
        }
    }

    public void add(String posterPath) {
        try {
            data.add(posterPath);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMovieData(String[] result) {
        if (result != null) {
            if (data != null) {
                Log.v(LOG_TAG, "reached here CustomMovieAdapter.setMovieData + " + result.length);
                for (String posterPath : result) {
                    String completePosterURL = IMAGEBASEURL + POSTERSIZE + posterPath;
                    data.add(completePosterURL);
                }
                notifyDataSetChanged();
                //MovieFragment.recyclerViewScrollToPosition();

                MovieFragment.restoreRecyclerViewState();


            }
        } else if (result == null && data != null) {
            data.clear();
            notifyDataSetChanged();
        }
    }

    class MoviesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageView;


        public MoviesViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.list_item_movies_imageview);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

}
