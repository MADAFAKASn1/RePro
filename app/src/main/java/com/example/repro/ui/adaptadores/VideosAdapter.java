package com.example.repro.ui.adaptadores;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.repro.R;
import com.example.repro.ui.modelo.Video;

import java.util.List;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.MyViewHolder> {
    private List<Video> mData;
    private Context context;
    private LayoutInflater inflater;
    private int currentPlayingPosition = -1;
    private MyViewHolder currentPlayingHolder = null;

    public VideosAdapter(List<Video> data, Context ctx) {
        super();
        this.mData = data;
        this.context = ctx;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.adaptador_personalizado_videos, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        Video item = mData.get(position);
        holder.textViewName.setText(item.getTitle());

        if (position == currentPlayingPosition) {
            holder.playPauseButton.setImageResource(R.drawable.icons8_pausa_30);
            holder.textViewName.setSelected(true);
        } else {
            holder.playPauseButton.setImageResource(R.drawable.icons8_play_30);
            holder.textViewName.setSelected(false);
        }

        holder.videoView.setVideoURI(Uri.parse(item.getUri()));
        holder.videoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setVideoScalingMode(mediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        });
        holder.playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPlayingPosition == holder.getAdapterPosition()) {
                    if (holder.videoView.isPlaying()) {
                        holder.videoView.pause();
                        holder.playPauseButton.setImageResource(R.drawable.icons8_play_30);
                        holder.textViewName.setSelected(false);
                    } else {
                        holder.videoView.start();
                        holder.playPauseButton.setImageResource(R.drawable.icons8_pausa_30);
                        holder.textViewName.setSelected(true);
                    }
                } else {
                    if (currentPlayingHolder != null) {
                        currentPlayingHolder.videoView.stopPlayback();
                        currentPlayingHolder.playPauseButton.setImageResource(R.drawable.icons8_play_30);
                    }
                    holder.videoView.setVideoURI(Uri.parse(item.getUri()));
                    holder.videoView.start();
                    holder.playPauseButton.setImageResource(R.drawable.icons8_pausa_30);
                    currentPlayingPosition = holder.getAdapterPosition();
                    currentPlayingHolder = holder;
                }
            }
        });
        holder.resta30seg.setOnClickListener(v -> {
            // Retroceder 30 segundos
            int currentPosition = holder.videoView.getCurrentPosition();
            int rewindPosition = currentPosition - 30000; // 30 segundos en milisegundos
            holder.videoView.seekTo(Math.max(rewindPosition, 0));
        });

        holder.suma30Seg.setOnClickListener(v -> {
            // Adelantar 30 segundos
            int currentPosition = holder.videoView.getCurrentPosition();
            int fastForwardPosition = currentPosition + 30000; // 30 segundos en milisegundos
            holder.videoView.seekTo(Math.min(fastForwardPosition, holder.videoView.getDuration()));
        });

        holder.videoView.setOnCompletionListener(mp -> {
            holder.playPauseButton.setImageResource(R.drawable.icons8_play_30);
            currentPlayingPosition = -1;
            currentPlayingHolder = null;
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
        ImageButton resta30seg;
        ImageButton suma30Seg;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            videoView = itemView.findViewById(R.id.videoView);
            playPauseButton = itemView.findViewById(R.id.play_pause);
            resta30seg = itemView.findViewById(R.id.rest30seg);
            suma30Seg = itemView.findViewById(R.id.sum30seg);
        }
    }
}
