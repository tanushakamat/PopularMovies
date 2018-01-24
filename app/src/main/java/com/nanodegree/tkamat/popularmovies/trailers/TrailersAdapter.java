package com.nanodegree.tkamat.popularmovies.trailers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nanodegree.tkamat.popularmovies.DetailActivityFragment;
import com.nanodegree.tkamat.popularmovies.R;
import com.squareup.picasso.Picasso;

/**
 * Created by tnadkarn on 8/3/2017.
 */

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailersViewHolder> {

    private String[]  mTrailerData;
    Context context;
    private static final String YOUTUBETHUMBNAILSTART = "https://img.youtube.com/vi/";
    private static final String YOUTUBETHUMBNAILEND = "/0.jpg";
    private static final String YOUTUBELINKSTART = "https://www.youtube.com/watch?v=";
    private static ListItemClickListener mOnClickListener;

    public static boolean isTrailerDataSet;

    public TrailersAdapter(ListItemClickListener listener)
    {
        this.mOnClickListener = listener;
    }

    final String LOG_TAG = TrailersAdapter.class.getSimpleName();
    @Override
    public TrailersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.v(LOG_TAG, "reached here TrailersAdapter.onCreateViewHolder" );

        context = parent.getContext();
        int layoutIdForListItem = R.layout.trailer_list_item;
        boolean shouldAttachToParentImmediately = false;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(layoutIdForListItem, parent , shouldAttachToParentImmediately);
        TrailersViewHolder trailersViewHolder = new TrailersViewHolder(view);

        return trailersViewHolder;
    }

    @Override
    public void onBindViewHolder(TrailersViewHolder trailersViewHolder, int position) {
        Log.v(LOG_TAG, "reached here TrailersAdapter.onBindViewHolder" );

        String imageThumbnailUrl = YOUTUBETHUMBNAILSTART + mTrailerData[position] + YOUTUBETHUMBNAILEND;
        trailersViewHolder.trailerYoutubeLink = YOUTUBELINKSTART + mTrailerData[position];
        trailersViewHolder.trailerLinkView.setText("Trailer" + (position+1));
        Log.v(LOG_TAG, imageThumbnailUrl);


        try {
            Picasso
                    .with(context)
                    .load(imageThumbnailUrl)
                    .into((ImageView) trailersViewHolder.trailerThumbnailView);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Log.v(LOG_TAG, "trailerLink = " + trailerLink);
    }



    public void setTrailerData(String[] trailerLinks)
    {
        mTrailerData = trailerLinks;
        Log.v(LOG_TAG, "reached here TrailersAdapter.setTrailerData" );
        DetailActivityFragment.restoreScrollPosition();
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        if(mTrailerData == null)return 0;
        else
            return mTrailerData.length;
    }



    class TrailersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView trailerLinkView;
        public ImageView trailerThumbnailView;
        String trailerYoutubeLink;

        public  TrailersViewHolder(View itemView)
        {
            super(itemView);
            trailerLinkView = (TextView) itemView.findViewById(R.id.trailer_link);
            trailerThumbnailView = (ImageView) itemView.findViewById(R.id.trailerThumbnail);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            Log.v(LOG_TAG, "Reached Here: TrailersViewHolder.onClick" + trailerYoutubeLink);
            mOnClickListener.onListItemClick(clickedPosition, trailerYoutubeLink);
        }
    }

    public interface ListItemClickListener
    {
        void onListItemClick(int clickedItemIndex, String trailerYoutubeLink);
    }
}
