package com.example.repro.ui.inicio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.repro.R;
import com.example.repro.databinding.FragmentInicioBinding;
import com.example.repro.ui.adaptadores.AdaptadorPersonalizado;
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
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
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
                        loadVideos();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return root;
    }

    private void loadCanciones() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        System.out.println("currentUser " + currentUser);
        if (currentUser != null) {
            StorageReference cancionesRef = mStorageRef.child("canciones");
            System.out.println("cancionesRef " + cancionesRef);
            cancionesRef.listAll()
                    .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            System.out.println("cancionesRef.listAll() " + cancionesRef.listAll());
                            List<StorageReference> data = new ArrayList<>();
                            for (StorageReference fileRef : listResult.getItems()) {
                                System.out.println("fileRef " + fileRef);
                                data.add(fileRef);
                            }
                            AdaptadorPersonalizado adapter = new AdaptadorPersonalizado(data, getContext());
                            recyclerView.setAdapter(adapter);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            System.out.println("Error loadCanciones " + exception.getMessage());
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
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
                            AdaptadorPersonalizado adapter = new AdaptadorPersonalizado(data, getContext());
                            recyclerView.setAdapter(adapter);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            System.out.println("Error loadVideos " + exception.getMessage());
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}