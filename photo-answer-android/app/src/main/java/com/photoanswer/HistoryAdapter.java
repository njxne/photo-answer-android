package com.photoanswer;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.List;
import java.util.Map;

/** 历史记录列表。 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {

    public interface Listener {
        void onClick(Map<String, Object> item);

        void onLongClick(Map<String, Object> item);
    }

    private final List<Map<String, Object>> data;
    private final Listener listener;

    public HistoryAdapter(List<Map<String, Object>> data, Listener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Map<String, Object> m = data.get(position);
        h.meta.setText(Ui.modeLabel(Json.str(m.get("mode")))
                + " · " + Json.str(m.get("grade"))
                + " · " + Ui.time(Ui.asLong(m.get("t"))));

        String q = Json.str(m.get("q"));
        h.text.setText(q == null || q.isEmpty() ? "(无文字)" : q);

        String img = Json.str(m.get("img"));
        File f = (img == null || img.isEmpty()) ? null : new File(img);
        if (f != null && f.exists()) {
            h.thumbCard.setVisibility(View.VISIBLE);
            h.thumb.setImageBitmap(BitmapFactory.decodeFile(img));
        } else {
            h.thumbCard.setVisibility(View.GONE);
            h.thumb.setImageDrawable(null);
        }

        h.itemView.setOnClickListener(v -> listener.onClick(m));
        h.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(m);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final MaterialCardView thumbCard;
        final ImageView thumb;
        final TextView meta;
        final TextView text;

        VH(View v) {
            super(v);
            thumbCard = v.findViewById(R.id.historyThumbCard);
            thumb = v.findViewById(R.id.historyThumb);
            meta = v.findViewById(R.id.historyMeta);
            text = v.findViewById(R.id.historyText);
        }
    }
}
