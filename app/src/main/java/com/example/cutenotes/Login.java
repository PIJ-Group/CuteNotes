package com.example.cutenotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class Login extends AppCompatActivity {

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

        botonLogin = findViewById(R.id.botonLogin);
        botonLogin.setOnClickListener(view -> {
            //LOGIN EN FIREBASE
            String email = emailText.getText().toString();
            String password = passText.getText().toString();

            if(email.isEmpty()){
                emailText.setError(getString(R.string.campo_vacio));
            }else if(!email.contains("@") || !email.contains(".") || email.contains(" ")){
                emailText.setError(getString(R.string.mail_no_valido));
            }else if(password.isEmpty()){
                passText.setError(getString(R.string.campo_vacio));
            }else {

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

            if(email.isEmpty()){
                emailText.setError(getString(R.string.campo_vacio));
            }else if(!email.contains("@") || !email.contains(".") || email.contains(" ")){
                emailText.setError(getString(R.string.mail_no_valido));
            }else if(password.isEmpty()){
                passText.setError(getString(R.string.campo_vacio));
            }else if(password.length() < 6){
                passText.setError(getString(R.string.contraseña_corta));

            }else {

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

    }

    public void toastOk(String msg){
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.toast_ok, (ViewGroup) findViewById(R.id.custom_ok));
        TextView txtMensaje = view.findViewById(R.id.text_ok);
        txtMensaje.setText(msg);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0,200);
        toast.setDuration (Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();
    }

    public void toastWarning(String msg){
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.toast_warning, (ViewGroup) findViewById(R.id.custom_warning));
        TextView txtMensaje = view.findViewById(R.id.text_warning);
        txtMensaje.setText(msg);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0,200);
        toast.setDuration (Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();
    }

}