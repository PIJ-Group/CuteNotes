package com.example.cutenotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class Login extends AppCompatActivity {
    static final int RC_SIGN_IN = 1;
    GoogleSignInClient mGoogleSignInClient;
    Button botonGoogle;
    Button botonLogin;
    TextView botonRegistro;
    EditText emailText, passText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Objects.requireNonNull(getSupportActionBar()).hide();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        emailText = findViewById(R.id.cajaCorreo);
        passText = findViewById(R.id.cajaPass);

        botonGoogle = findViewById(R.id.botonGoogle);
        botonLogin = findViewById(R.id.botonLogin);
        botonLogin.setOnClickListener(view -> {
            //LOGIN EN FIREBASE
            String email = emailText.getText().toString();
            String password = passText.getText().toString();

            if (email.isEmpty()) {
                emailText.setError(getString(R.string.campo_vacio));
            } else if (!email.contains("@") || !email.contains(".") || email.contains(" ")) {
                emailText.setError(getString(R.string.mail_no_valido));
            } else if (password.isEmpty()) {
                passText.setError(getString(R.string.campo_vacio));
            } else {

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Intent intent = new Intent(Login.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                // If sign in fails, display a message to the user.
                                toastWarning(getString(R.string.autenticacion_fallida));

                            }
                        });
            }

        });

        botonRegistro = findViewById(R.id.botonRegistro);
        botonRegistro.setOnClickListener(view -> {
            //CREAR USUARIO EN FIREBASE
            String email = emailText.getText().toString();
            String password = passText.getText().toString();

            if (email.isEmpty()) {
                emailText.setError(getString(R.string.campo_vacio));
            } else if (!email.contains("@") || !email.contains(".") || email.contains(" ")) {
                emailText.setError(getString(R.string.mail_no_valido));
            } else if (password.isEmpty()) {
                passText.setError(getString(R.string.campo_vacio));
            } else if (password.length() < 6) {
                passText.setError(getString(R.string.contraseña_corta));

            } else {

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                toastOk(getString(R.string.usuario_creado));

                                Intent intent = new Intent(Login.this, MainActivity.class);
                                startActivity(intent);

                            } else {
                                // If sign in fails, display a message to the user.

                                toastWarning(getString(R.string.usuario_existente));

                            }
                        });
            }
        });

        //-------------------Google------------------------//
        //Configuración del inicio de sesión en Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        botonGoogle.setOnClickListener(view -> signIn());
        // [END config_signin]

    }

    //Método para inciar sesión
    public void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    //Método para verificar el resultado de la actividad de loguearse
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
            }
        }
    }


    //Método para comprobar el token e iniciar sesión si este es válido
    public void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        goHome();
                        Login.this.finish();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        updateUI(null);
                    }
                });
    }
    //Método para verificar el estado del usuario actual y movernos entre actividades
    private void updateUI(FirebaseUser user) {
        user = mAuth.getCurrentUser();
        if (user != null) {
            goHome();
        }

    }
    // [END auth_with_google]

    // Método para moverse entre actividades
    private void goHome() {
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
    }

    public void toastOk(String msg) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.toast_ok, findViewById(R.id.custom_ok));
        TextView txtMensaje = view.findViewById(R.id.text_ok);
        txtMensaje.setText(msg);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();
    }

    public void toastWarning(String msg) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.toast_warning, findViewById(R.id.custom_warning));
        TextView txtMensaje = view.findViewById(R.id.text_warning);
        txtMensaje.setText(msg);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();
    }


}