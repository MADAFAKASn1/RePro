package com.example.repro.ui.iniciar;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.repro.MainActivity;
import com.example.repro.databinding.ActivityIniciarSesionBinding;
import com.example.repro.ui.modelo.Usuario;
import com.example.repro.ui.registrar.Registro;
import com.example.repro.ui.repositorio.FirebaseImple;
import com.example.repro.utils.InternetUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class IniciarSesion extends AppCompatActivity {
    private static final int RC_SIGN_IN = 0001;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;
    private ActivityIniciarSesionBinding binding;
    private GoogleSignInClient mGoogleSignInClient;
    private static Usuario inicioSesionUsuario;
    private GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("838694692089-dtqrqv7747vnktm959ppgl92qdmeafsg.apps.googleusercontent.com")
            .requestEmail()
            .build();
    private FirebaseImple firebaseImple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIniciarSesionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        firebaseImple = new FirebaseImple();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding.iniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.email.getText().toString().trim();
                String password = binding.password.getText().toString().trim();
                if (email.isEmpty() || password.isEmpty()) {
                    // Si el correo electrónico o la contraseña están vacíos, muestra un mensaje de error
                    Toast.makeText(IniciarSesion.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Inicio de sesión exitoso
                                    Toast.makeText(IniciarSesion.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                                    Thread hilo = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                firebaseImple.recuperarUsuario(email);
                                                // Espera 2 segundos
                                                Thread.sleep(2000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            // Luego de esperar 2 segundos, ejecuta el código
                                            Intent intent = new Intent(IniciarSesion.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });

                                    hilo.start();
                                } else {
                                    // Error al iniciar sesión
                                    Toast.makeText(IniciarSesion.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                }
            }
        });
        binding.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        binding.crearCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(IniciarSesion.this, Registro.class);
                startActivity(i);
            }
        });

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Thread hilo = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            firebaseImple.recuperarUsuario(account.getEmail());
                            // Espera 2 segundos
                            Thread.sleep(2000);
                            if(inicioSesionUsuario != null){
                                Intent intent = new Intent(IniciarSesion.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }else{
                                firebaseImple.registrarNuevoUsuario(account.getEmail());
                                firebaseImple.recuperarUsuario(account.getEmail());
                                Thread.sleep(2000);
                                Intent intent = new Intent(IniciarSesion.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        firebaseAuthWithGoogle(account.getIdToken());
                    }
                });

                hilo.start();
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(IniciarSesion.this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }
    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sin conexión a Internet")
                .setMessage("Necesitas conectarte a Internet para usar esta aplicación.")
                .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    }

    public static Usuario getInicioSesionUsuario() {
        return inicioSesionUsuario;
    }

    public static void setInicioSesionUsuario(Usuario inicioSesionUsuario) {
        IniciarSesion.inicioSesionUsuario = inicioSesionUsuario;
    }
}