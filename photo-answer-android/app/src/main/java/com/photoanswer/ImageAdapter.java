package com.photoanswer;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** 已选图片的横向缩略图列表。 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.VH> {

    public interface OnRemove {
        void onRemove(int position);
    }

    private final List<Uri> data;
    private final OnRemove onRemove;

    public ImageAdapter(List<Uri> data, OnRemove onRemove) {
        this.data = data;
        this.onRemove = onRemove;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_thumb, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Uri uri = data.get(position);
        h.image.setImageBitmap(ImageUtil.thumb(h.image.getContext(), uri, 220));
        h.remove.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) onRemove.onRemove(pos);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView image;
        final ImageButton remove;

        VH(View v) {
            super(v);
            image = v.findViewById(R.id.thumbImage);
            remove = v.findViewById(R.id.btnRemoveImage);
        }
    }
}
