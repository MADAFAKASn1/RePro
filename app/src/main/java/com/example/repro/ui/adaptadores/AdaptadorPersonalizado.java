package com.example.repro.ui.adaptadores;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.repro.R;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;

public class AdaptadorPersonalizado extends RecyclerView.Adapter<AdaptadorPersonalizado.MyViewHolder> {
    private List<StorageReference> mData;
    private MediaPlayer mediaPlayer;
    private int currentPlayingPosition = -1;
    private MyViewHolder currentPlayingHolder = null;
    private Context context;
    private LayoutInflater inflater;

    public AdaptadorPersonalizado(List<StorageReference> data, Context ctx) {
        super();
        this.mData = data;
        this.context = ctx;
        this.mediaPlayer = new MediaPlayer();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.adaptador_personalizado_inicio, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        StorageReference item = mData.get(position);
        holder.textViewName.setText(item.getName());

        holder.playPauseButton.setImageResource(
                holder.getAdapterPosition() == currentPlayingPosition ?
                        R.drawable.icons8_pausa_30 : R.drawable.icons8_play_30
        );

        holder.playPauseButton.setOnClickListener(v -> {
            if (currentPlayingPosition == holder.getAdapterPosition()) {
                if (mediaPlayer.isPlaying()) {
                    pauseAudio();
                    currentPlayingHolder.textViewName.setSelected(false);
                } else {
                    resumeAudio();
                    currentPlayingHolder.textViewName.setSelected(true);
                }
            } else {
                playAudio(holder, item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private void playAudio(MyViewHolder holder, StorageReference item) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            if (currentPlayingHolder != null) {
                currentPlayingHolder.playPauseButton.setImageResource(R.drawable.icons8_play_30);
                currentPlayingHolder.textViewName.setSelected(false); // Stop marquee effect on previous song's TextView
            }
        }
        mediaPlayer.reset();

        currentPlayingPosition = holder.getAdapterPosition();
        currentPlayingHolder = holder;

        item.getDownloadUrl().addOnSuccessListener(uri -> {
            try {
                mediaPlayer.setDataSource(uri.toString());
                mediaPlayer.prepare();
                mediaPlayer.start();
                holder.playPauseButton.setImageResource(R.drawable.icons8_pausa_30);
                holder.textViewName.setSelected(true); // Start marquee effect on current song's TextView
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            holder.playPauseButton.setImageResource(R.drawable.icons8_play_30);
            currentPlayingPosition = -1;
            currentPlayingHolder = null;
        });
    }

    private void pauseAudio() {
        mediaPlayer.pause();
        if (currentPlayingHolder != null) {
            currentPlayingHolder.playPauseButton.setImageResource(R.drawable.icons8_play_30);
        }
    }

    private void resumeAudio() {
        mediaPlayer.start();
        if (currentPlayingHolder != null) {
            currentPlayingHolder.playPauseButton.setImageResource(R.drawable.icons8_pausa_30);
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        ImageView imageView;
        ImageButton playPauseButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            imageView = itemView.findViewById(R.id.imageViewFondo);
            playPauseButton = itemView.findViewById(R.id.play_pause);
        }
    }
}
