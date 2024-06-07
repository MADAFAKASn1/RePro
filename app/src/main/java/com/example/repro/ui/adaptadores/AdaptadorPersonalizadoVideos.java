package com.example.repro.ui.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.repro.R;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class AdaptadorPersonalizadoVideos extends RecyclerView.Adapter<AdaptadorPersonalizadoVideos.MyViewHolder> {
    private List<StorageReference> mData;
    private Context context;
    private LayoutInflater inflater;

    public AdaptadorPersonalizadoVideos(List<StorageReference> data, Context ctx) {
        this.mData = data;
        this.context = ctx;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.adaptador_personalizado_videos, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        StorageReference item = mData.get(position);
        holder.textViewName.setText(item.getName());

        item.getDownloadUrl().addOnSuccessListener(uri -> {
            holder.videoView.setVideoURI(uri);
            holder.videoView.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                holder.playPauseButton.setOnClickListener(v -> {
                    if (holder.videoView.isPlaying()) {
                        holder.videoView.pause();
                        holder.playPauseButton.setImageResource(R.drawable.icons8_play_30);
                        holder.textViewName.setSelected(false);
                    } else {
                        holder.videoView.start();
                        holder.playPauseButton.setImageResource(R.drawable.icons8_pausa_30);
                        holder.textViewName.setSelected(true);
                    }
                });

                holder.anterior.setOnClickListener(v -> {
                    // Retroceder 30 segundos
                    int currentPosition = holder.videoView.getCurrentPosition();
                    int rewindPosition = currentPosition - 30000; // 30 segundos en milisegundos
                    holder.videoView.seekTo(Math.max(rewindPosition, 0));
                });

                holder.siguiente.setOnClickListener(v -> {
                    // Adelantar 30 segundos
                    int currentPosition = holder.videoView.getCurrentPosition();
                    int fastForwardPosition = currentPosition + 30000; // 30 segundos en milisegundos
                    holder.videoView.seekTo(Math.min(fastForwardPosition, holder.videoView.getDuration()));
                });
            });
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        VideoView videoView;
        ImageButton playPauseButton;
        ImageButton anterior;
        ImageButton siguiente;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            videoView = itemView.findViewById(R.id.videoView);
            playPauseButton = itemView.findViewById(R.id.play_pause);
            anterior = itemView.findViewById(R.id.anterior);
            siguiente = itemView.findViewById(R.id.siguiente);
        }
    }
}