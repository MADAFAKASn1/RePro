package com.example.repro.ui.canciones;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.repro.databinding.FragmentCancionesBinding;
import com.example.repro.ui.adaptadores.CancionesAdapter;
import com.example.repro.ui.modelo.Cancion;
import com.example.repro.ui.servicios.MusicService;

import java.util.ArrayList;
import java.util.List;

public class CancionesFragment extends Fragment {

    private FragmentCancionesBinding binding;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private RecyclerView recyclerView;
    private List<Cancion> cancionList;
    private CancionesAdapter adaptadorCanciones;
    private BroadcastReceiver musicCompletionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MusicService.ACTION_COMPLETED.equals(intent.getAction())) {
                if (adaptadorCanciones != null) {
                    adaptadorCanciones.releaseMediaPlayer();
                }
            }
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCancionesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerViewCanciones;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cancionList = new ArrayList<>();

        // Crear una instancia del adaptador y establecerlo en el RecyclerView
        adaptadorCanciones = new CancionesAdapter(cancionList, getContext());
        recyclerView.setAdapter(adaptadorCanciones);

        // Verificar y solicitar permisos si es necesario
        checkAndRequestPermissions();

        // Registrar el receptor
        IntentFilter filter = new IntentFilter(MusicService.ACTION_COMPLETED);
        getContext().registerReceiver(musicCompletionReceiver, filter);

        return root;
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_AUDIO)) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
                Toast.makeText(getContext(), "El permiso es necesario para cargar las canciones.", Toast.LENGTH_LONG).show();
            }

            // Solicitar el permiso
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
        } else {
            // Permiso ya concedido
            loadSongs();
        }
    }

    private void loadSongs() {
        ContentResolver contentResolver = requireContext().getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA
        };
        Cursor songCursor = contentResolver.query(songUri, projection, null, null, null);

        if (songCursor != null && songCursor.moveToFirst()) {
            int idColumn = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int uriColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do {
                long id = songCursor.getLong(idColumn);
                String title = songCursor.getString(titleColumn);
                String uri = songCursor.getString(uriColumn);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                cancionList.add(new Cancion(title, uri)); // Crear un objeto Cancion y agregarlo a la lista
            } while (songCursor.moveToNext());

            songCursor.close();

            // Notificar al adaptador que los datos han cambiado
            adaptadorCanciones.notifyDataSetChanged();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, puedes proceder con la carga de canciones
                loadSongs();
            } else {
                // Permiso denegado, solicitar de nuevo el permiso
                checkAndRequestPermissions();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adaptadorCanciones != null) {
            adaptadorCanciones.releaseMediaPlayer();
        }
        getContext().unregisterReceiver(musicCompletionReceiver);
        binding = null;
    }
}
