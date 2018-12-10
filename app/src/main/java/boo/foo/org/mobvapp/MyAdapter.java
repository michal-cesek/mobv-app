package boo.foo.org.mobvapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ViewTarget;

import boo.foo.org.mobvapp.models.Post;

import static com.crashlytics.android.core.CrashlyticsCore.TAG;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private Post[] mDataset;
    private Context postContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView postUser;
        public TextView postDate;
        public ImageView postContent;


        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public MyViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);


            postUser = (TextView) itemView.findViewById(R.id.post_user);
            postDate = (TextView) itemView.findViewById(R.id.post_date);
            postContent = (ImageView) itemView.findViewById(R.id.post_content);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(Post[] post, Context context) {
        mDataset = post;
        postContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_post, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.postUser.setText(mDataset[position].getUsername() + " " + mDataset[position].getType());
        holder.postDate.setText(mDataset[position].getDate().toDate().toString());
        Log.d(TAG, mDataset[position].getType());
        if (mDataset[position].getType() == "image")
        {
            //holder.postContent.setima(mDataset[position].getImageurl());
            String url = mDataset[position].getImageurl();
            Glide.with(postContext.getApplicationContext())
                    .load(url)
                    .into(holder.postContent);
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}