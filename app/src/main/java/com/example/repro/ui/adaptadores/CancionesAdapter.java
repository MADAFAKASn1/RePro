package com.example.repro.ui.adaptadores;

import static com.example.repro.ui.servicios.MusicService.mediaPlayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.repro.R;
import com.example.repro.ui.modelo.Cancion;

import java.io.IOException;
import java.util.List;

public class CancionesAdapter extends RecyclerView.Adapter<CancionesAdapter.MyViewHolder> {
    private List<Cancion> mData;
    private Context context;
    private LayoutInflater inflater;
    private MediaPlayer mediaPlayer;
    private int currentPlayingPosition = -1;
    private MyViewHolder currentPlayingHolder = null;

    public CancionesAdapter(List<Cancion> data, Context ctx) {
        super();
        this.mData = data;
        this.context = ctx;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.adaptador_personalizado_canciones, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        Cancion item = mData.get(position);
        holder.textViewName.setText(item.getTitle());
        if (position == currentPlayingPosition) {
            holder.playPauseButton.setImageResource(R.drawable.icons8_pausa_30);
        } else {
            holder.playPauseButton.setImageResource(R.drawable.icons8_play_30);
        }
        holder.playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPlayingPosition == holder.getAdapterPosition()) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        holder.playPauseButton.setImageResource(R.drawable.icons8_play_30);
                    } else {
                        mediaPlayer.start();
                        holder.playPauseButton.setImageResource(R.drawable.icons8_pausa_30);
                    }
                } else {
                    try {
                        // Detener la reproducción de la canción actual si hay una
                        if (currentPlayingHolder != null) {
                            currentPlayingHolder.playPauseButton.setImageResource(R.drawable.icons8_play_30);
                        }
                        playAudio(holder, item);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        holder.resta30seg.setOnClickListener(v -> {
            if (currentPlayingPosition == holder.getAdapterPosition() && mediaPlayer != null) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int rewindPosition = currentPosition - 30000; // 30 segundos en milisegundos
                mediaPlayer.seekTo(Math.max(rewindPosition, 0));
            }
        });

        holder.suma30Seg.setOnClickListener(v -> {
            if (currentPlayingPosition == holder.getAdapterPosition() && mediaPlayer != null) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int fastForwardPosition = currentPosition + 30000; // 30 segundos en milisegundos
                mediaPlayer.seekTo(Math.min(fastForwardPosition, mediaPlayer.getDuration()));
            }
        });
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    private void playAudio(MyViewHolder holder, Cancion cancion) throws IOException {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        } else {
            mediaPlayer.reset();
        }

        mediaPlayer.setDataSource(context, Uri.parse(cancion.getUri()));
        mediaPlayer.prepare();
        mediaPlayer.start();
        holder.playPauseButton.setImageResource(R.drawable.icons8_pausa_30);
        currentPlayingPosition = holder.getAdapterPosition();
        currentPlayingHolder = holder;

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.reset();
                holder.playPauseButton.setImageResource(R.drawable.icons8_play_30);
                currentPlayingPosition = -1;
                currentPlayingHolder = null;
            }
        });
    }

    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        ImageButton playPauseButton;
        ImageButton resta30seg;
        ImageButton suma30Seg;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            playPauseButton = itemView.findViewById(R.id.play_pause);
            resta30seg = itemView.findViewById(R.id.rest30seg);
            suma30Seg = itemView.findViewById(R.id.sum30seg);
        }
    }
}
