package com.example.repro.ui.inicio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.repro.R;
import com.example.repro.databinding.FragmentInicioBinding;
import com.example.repro.ui.adaptadores.AdaptadorPersonalizadoCanciones;
import com.example.repro.ui.adaptadores.AdaptadorPersonalizadoVideos;
import com.example.repro.ui.servicios.MusicService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;
    private RecyclerView recyclerView;
    private Spinner spinner;
    private StorageReference mStorageRef;
    private FirebaseAuth firebaseAuth;
    private AdaptadorPersonalizadoCanciones adaptadorCanciones;
    private AdaptadorPersonalizadoVideos adaptadorVideos;

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
        binding = FragmentInicioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        recyclerView = binding.recyclerViewInicio;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));  // Añadir LayoutManager

        spinner = binding.spinner;

        //Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.spinner_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //Listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // Canciones
                        loadCanciones();
                        break;
                    case 1: // Videos
                        if (adaptadorCanciones != null) {
                            adaptadorCanciones.releaseMediaPlayer();
                        }
                        loadVideos();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Registrar el receptor
        IntentFilter filter = new IntentFilter(MusicService.ACTION_COMPLETED);
        getContext().registerReceiver(musicCompletionReceiver, filter);

        return root;
    }

    private void loadCanciones() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            StorageReference cancionesRef = mStorageRef.child("canciones");
            cancionesRef.listAll()
                    .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            List<StorageReference> data = new ArrayList<>();
                            for (StorageReference fileRef : listResult.getItems()) {
                                data.add(fileRef);
                            }
                            adaptadorCanciones = new AdaptadorPersonalizadoCanciones(data, getContext());
                            recyclerView.setAdapter(adaptadorCanciones);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            System.out.println("Error loadCanciones " + exception.getMessage());
                        }
                    });
        } else {
            System.out.println("Usuario no autenticado");
        }
    }


    private void loadVideos() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            StorageReference videosRef = mStorageRef.child("videos");
            videosRef.listAll()
                    .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            List<StorageReference> data = new ArrayList<>();
                            for (StorageReference fileRef : listResult.getItems()) {
                                data.add(fileRef);
                            }
                            adaptadorVideos = new AdaptadorPersonalizadoVideos(data, getContext());
                            recyclerView.setAdapter(adaptadorVideos);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            System.out.println("Error loadVideos " + exception.getMessage());
                        }
                    });
        } else {
            System.out.println("Usuario no autenticado");
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
