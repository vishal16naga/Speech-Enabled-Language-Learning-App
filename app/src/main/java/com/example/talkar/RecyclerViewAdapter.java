package com.example.talkar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import javax.annotation.Nonnull;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private static int TYPE_LOCKED = 1;
    private static int TYPE_UNLOCKED = 2;

    int []arr;
    int alphabetsCompleted;

    public RecyclerViewAdapter(int[] arr, int alphabetsCompleted) {
        this.arr = arr;
        this.alphabetsCompleted = alphabetsCompleted;
    }


    @Override
    public MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_UNLOCKED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.unlocked_single_view, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }
        else if (viewType == TYPE_LOCKED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.locked_single_view, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.locked_single_view, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@Nonnull MyViewHolder holder, int position) {
        holder.imageView.setImageResource(arr[position]);
    }

    @Override
    public int getItemViewType(int position) {
        if (alphabetsCompleted > position){
            return TYPE_UNLOCKED;
        }
        else {
            return TYPE_LOCKED;
        }
    }

    @Override
    public int getItemCount() {
        return arr.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public MyViewHolder(@Nonnull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);

        }
    }

}
