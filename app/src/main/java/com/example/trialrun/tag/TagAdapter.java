package com.example.trialrun.tag;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.MyViewHolder> {

    private final List<Tag> mDataset;

    // Provide a reference to the views for each data item
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public final View view;

        public MyViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    // TagAdapter class to display Tag objects in a list
    public TagAdapter() {
        mDataset = new ArrayList<>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        // create a new view
        return new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tag, parent, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        try {
            ((TextView) (holder.view.findViewById(R.id.tvName))).setText(mDataset.get(position).getEpcMem());
            ((TextView) (holder.view.findViewById(R.id.tvTime))).setText(mDataset.get(position).getTimestamp().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void update(Tag tag) {
        int pos = mDataset.indexOf(tag);
        if (pos > -1) {
            mDataset.set(pos, tag);
            notifyOnMainThread(2, pos);
        }
    }

    public void addItem(Tag tag) {
        mDataset.add(mDataset.size(), tag);
        notifyOnMainThread(1, mDataset.size() - 1);
    }

    public void removeOldTags(long olderThan) {
        Timestamp timeNow = new Timestamp(new Date().getTime());
        List<Tag> listToRemove = mDataset.stream().filter(tag -> (timeNow.getTime() - tag.getTimestamp().getTime()) > olderThan).collect(Collectors.toList());
        mDataset.removeAll(listToRemove);

        notifyOnMainThread(0, -1);
    }

    public void checkTag(Tag tag) {
        boolean isListed = mDataset.contains(tag);

        if (isListed) {
            update(tag);
        } else {
            addItem(tag);
        }
        mDataset.indexOf(tag);
    }

    public void notifyOnMainThread(int type, int position) {
        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());

        Runnable myRunnable = () -> {
            switch (type) {
                case 1 -> notifyItemInserted(position);
                case 2 -> notifyItemChanged(position);
                default -> notifyDataSetChanged();
            }
        };
        mainHandler.post(myRunnable);
    }
}
