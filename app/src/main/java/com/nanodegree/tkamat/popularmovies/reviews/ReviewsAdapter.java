package com.nanodegree.tkamat.popularmovies.reviews;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nanodegree.tkamat.popularmovies.R;

import java.util.ArrayList;

/**
 * Created by tnadkarn on 8/3/2017.
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder> {

    private ArrayList<ReviewData> mReviewData;
    Context context;
    private static ListItemClickListener mOnClickListener;

    public ReviewsAdapter(ListItemClickListener listener)
    {
        this.mOnClickListener = listener;
    }

    final String LOG_TAG = ReviewsAdapter.class.getSimpleName();
    @Override
    public ReviewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.v(LOG_TAG, "reached here ReviewsAdapter.onCreateViewHolder" );

        context = parent.getContext();
        int layoutIdForListItem = R.layout.review_list_item;
        boolean shouldAttachToParentImmediately = false;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(layoutIdForListItem, parent , shouldAttachToParentImmediately);
        ReviewsViewHolder reviewsViewHolder = new ReviewsViewHolder(view);

        return reviewsViewHolder;
    }

    @Override
    public void onBindViewHolder(ReviewsViewHolder reviewsViewHolder, int position) {
        Log.v(LOG_TAG, "reached here ReviewsAdapter.onBindViewHolder" );
        reviewsViewHolder.reviewContentView.setText(mReviewData.get(position).getContent());
        reviewsViewHolder.reviewAuthorView.setText(mReviewData.get(position).getAuthor());
        reviewsViewHolder.reviewUrlView.setText(mReviewData.get(position).getUrl());

    }



    public void setReviewData(ArrayList<ReviewData> reviewData)
    {
        mReviewData = reviewData;
        Log.v(LOG_TAG, "reached here ReviewsAdapter.setReviewData, reviewData.length = " + reviewData.size() );
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        if(mReviewData == null)return 0;
        else
            return mReviewData.size();
    }



    class ReviewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView reviewContentView;
        public TextView reviewAuthorView;
        public TextView reviewUrlView;

        public ReviewsViewHolder(View itemView)
        {
            super(itemView);
            reviewContentView = (TextView) itemView.findViewById(R.id.review_content);
            reviewAuthorView = (TextView) itemView.findViewById(R.id.review_author);
            reviewUrlView = (TextView) itemView.findViewById(R.id.review_url);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onReviewListItemClick(clickedPosition, "");
        }
    }

    public interface ListItemClickListener
    {
        void onReviewListItemClick(int clickedItemIndex, String trailerYoutubeLink);
    }
}
