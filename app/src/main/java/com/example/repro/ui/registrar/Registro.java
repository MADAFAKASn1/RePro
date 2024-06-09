package com.example.repro.ui.registrar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.repro.databinding.ActivityRegistroBinding;
import com.example.repro.ui.iniciar.IniciarSesion;
import com.example.repro.ui.repositorio.FirebaseImple;
import com.example.repro.utils.InternetUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;


public class Registro extends AppCompatActivity {
    private ActivityRegistroBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseImple firebaseImple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        firebaseImple = new FirebaseImple();
        binding.registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.email.getText().toString().trim();
                String password = binding.password.getText().toString().trim();
                String confirmPassword = binding.passwordRepet.getText().toString().trim();

                // Verificar que no se deje ningún campo vacío
                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(Registro.this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Verificar que las contraseñas coincidan
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(Registro.this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Verificar la complejidad de la contraseña
                if (!isValidPassword(password)) {
                    Toast.makeText(Registro.this, "La contraseña debe tener al menos 8 caracteres + números.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Crear el usuario en Firebase Authentication
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // El usuario se ha creado exitosamente en Firebase Authentication
                                firebaseImple.registrarNuevoUsuario(email);
                                Toast.makeText(Registro.this, "Registro realizado", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(Registro.this, IniciarSesion.class);
                                startActivity(i);
                                finish();
                            } else {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(Registro.this, "El email ya está en uso.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Registro.this, "Error al registrar el usuario.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Registro.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });

    }

    // Método para verificar la complejidad de la contraseña
    private boolean isValidPassword(String password) {
        // La contraseña debe tener al menos 8 caracteres y contener letras y números
        return password.length() >= 8 && password.matches(".*[a-zA-Z].*") && password.matches(".*\\d.*");
    }

    @Override
    protected void onResume() {
        InternetUtil.isOnline(new InternetUtil.OnOnlineCheckListener() {
            @Override
            public void onResult(boolean isOnline) {
                if (!isOnline) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showNoInternetDialog();
                        }
                    });
                }
            }
        });
        super.onResume();
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sin conexión a Internet")
                .setMessage("Necesitas conectarte a Internet para usar esta aplicación.")
                .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}