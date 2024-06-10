package com.example.repro.ui.adaptadores;

import static com.example.repro.ui.servicios.MusicService.mediaPlayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
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
import com.example.repro.ui.servicios.MusicService;

import java.util.List;

public class CancionesAdapter extends RecyclerView.Adapter<CancionesAdapter.MyViewHolder> {
    private List<Cancion> mData;
    private Context context;
    private LayoutInflater inflater;
    private int currentPlayingPosition = -1;
    private MyViewHolder currentPlayingHolder = null;

    public CancionesAdapter(List<Cancion> data, Context ctx) {
        super();
        this.mData = data;
        this.context = ctx;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            holder.textViewName.setSelected(true);
        } else {
            holder.playPauseButton.setImageResource(R.drawable.icons8_play_30);
            holder.textViewName.setSelected(false);
        }
        holder.playPauseButton.setOnClickListener(v -> {
            if (currentPlayingPosition == holder.getAdapterPosition()) {
                Intent intent;
                if (mediaPlayer.isPlaying()) {
                    intent = new Intent(context, MusicService.class);
                    intent.setAction(MusicService.ACTION_PAUSE);
                    context.startService(intent);
                    holder.playPauseButton.setImageResource(R.drawable.icons8_play_30);
                    holder.textViewName.setSelected(false);
                } else {
                    intent = new Intent(context, MusicService.class);
                    intent.setAction(MusicService.ACTION_RESUME);
                    context.startService(intent);
                    holder.playPauseButton.setImageResource(R.drawable.icons8_pausa_30);
                    holder.textViewName.setSelected(true);
                }
            } else {
                playAudio(holder, item);
            }
        });

        holder.resta30seg.setOnClickListener(v -> {
            // Retroceder 30 segundos
            if (currentPlayingPosition == holder.getAdapterPosition() && mediaPlayer != null) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int rewindPosition = currentPosition - 30000; // 30 segundos en milisegundos
                mediaPlayer.seekTo(Math.max(rewindPosition, 0));
            }
        });

        holder.suma30Seg.setOnClickListener(v -> {
            // Adelantar 30 segundos
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

    private void playAudio(MyViewHolder holder, Cancion item) {
        if (currentPlayingHolder != null) {
            currentPlayingHolder.playPauseButton.setImageResource(R.drawable.icons8_play_30);
            currentPlayingHolder.textViewName.setSelected(false);
        }

        currentPlayingPosition = holder.getAdapterPosition();
        currentPlayingHolder = holder;

        // Obtener la URL de la canción
        String songUrl = item.getUri();

        // Crear el intent para iniciar el servicio MusicService
        Intent serviceIntent = new Intent(context, MusicService.class);
        serviceIntent.setAction(MusicService.ACTION_PLAY);
        serviceIntent.putExtra("songUrl", songUrl);

        // Iniciar el servicio dependiendo de la versión de Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        // Actualizar la interfaz de usuario
        holder.playPauseButton.setImageResource(R.drawable.icons8_pausa_30);
        holder.textViewName.setSelected(true);
    }

    public void releaseMediaPlayer() {
        Intent stopIntent = new Intent(context, MusicService.class);
        stopIntent.setAction(MusicService.ACTION_STOP);
        context.startService(stopIntent);

        if (currentPlayingHolder != null) {
            currentPlayingHolder.playPauseButton.setImageResource(R.drawable.icons8_play_30);
            currentPlayingHolder.textViewName.setSelected(false);
            currentPlayingHolder = null;
            currentPlayingPosition = -1;
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        ImageView imageView;
        ImageButton playPauseButton;
        ImageButton resta30seg;
        ImageButton suma30Seg;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            imageView = itemView.findViewById(R.id.imageViewFondo);
            playPauseButton = itemView.findViewById(R.id.play_pause);
            resta30seg = itemView.findViewById(R.id.rest30seg);
            suma30Seg = itemView.findViewById(R.id.sum30seg);
        }
    }
}
