package com.example.repro.ui.repositorio;

import android.util.Log;

import com.example.repro.ui.modelo.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FirebaseImple {
    public FirebaseImple() {
    }

    // Método para registrar un nuevo usuario
    public void registrarNuevoUsuario(String email) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Usuario nuevoObjeto = new Usuario(email);
        mDatabase.child("usuarios").push().setValue(nuevoObjeto, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e("RegistroUsuario", "Error al registrar el nuevo usuario: " + databaseError.getMessage());
                } else {
                    Log.d("RegistroUsuario", "Nuevo usuario registrado en Firebase");
                }
            }
        });
    }

    // Método para recuperar la información de un usuario
    public void recuperarUsuario(String email) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child("usuarios").orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot usuarioSnapshot : dataSnapshot.getChildren()) {
                    String emailUsuario = usuarioSnapshot.child("email").getValue(String.class);
                    System.out.println("Email del usuario: " + emailUsuario);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Error al recuperar el usuario: " + databaseError.getMessage());
            }
        });
    }
}