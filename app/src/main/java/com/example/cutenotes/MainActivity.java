package com.example.cutenotes;

import static com.example.cutenotes.R.string.empty_modified_task;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth nAuth;
    FirebaseFirestore db;
    String emailUser;
    ListView listViewTask;
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;
    List<String> taskList = new ArrayList<>();
    List<String> taskIdList = new ArrayList<>();
    ArrayAdapter<String> myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        db = FirebaseFirestore.getInstance();
        nAuth = FirebaseAuth.getInstance();
        emailUser = nAuth.getCurrentUser().getEmail();
        listViewTask = findViewById(R.id.listView);

        //Actualizar la interfaz de usuario con las tareas del usuario logueado
        updateUi();
        /*Inicializamos la variable gso que recogerá los elementos necesarios para que el usuario
          inicie sesión*/
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    private void updateUi() {
        db.collection("Tasks")
                .whereEqualTo("emailUser", emailUser)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {

                            return;
                        }
                        taskList.clear();
                        taskIdList.clear();

                        for (QueryDocumentSnapshot doc : value) {
                            taskIdList.add(doc.getId());
                            taskList.add(doc.getString("taskName"));
                        }
                        if (taskList.size() == 0) {
                            listViewTask.setAdapter(null);
                        } else {
                            myAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.item_tarea, R.id.item, taskList);
                            listViewTask.setAdapter(myAdapter);
                        }
                    }
                });
    }

    //Método para poder incluir el menú
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    //Método para poder incluir nuevas tareas y desloguearse.
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            //Case para añadir la tarea
            case R.id.newTask:

                //Activar el cuadro de diàlogo para añadir tareas
                final EditText taskEditText = new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.nueva_tarea)
                        .setMessage(R.string.tarea_pendiente)
                        .setView(taskEditText)
                        .setPositiveButton(R.string.añadir, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //Añadir tarea a la base de datos
                                String myTask = taskEditText.getText().toString();
                                Map<String, Object> task = new HashMap<>();
                                task.put("taskName", myTask);
                                task.put("emailUser", emailUser);

                                if(myTask.isEmpty()){
                                    //Toast de advertencia de tarea en blanco
                                    toastWarning(getString(R.string.introduce_tarea));

                                }else {
                                    //Añadir registro en la bbdd
                                    db.collection("Tasks").add(task);

                                    toastOk(getString(R.string.tarea_añadida));
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancelar, null)
                        .create();
                dialog.show();
                return true;

            //Case para desloguearse
            case R.id.logout:
                //Cierre de sesión firebase
                nAuth.signOut();

                //Cierre de sesión de google a través del método signOut y transición al login
                mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Intent loginActivity = new Intent(getApplicationContext(),Login.class);
                            startActivity(loginActivity);
                            MainActivity.this.finish();

                        }else{
                            toastWarning(getString(R.string.error_cierre_google));
                        }
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    //Método para modificar las tareas activas de cada usuario
    public void modifyTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTv = parent.findViewById(R.id.item);
        String taskContent = taskTv.getText().toString();

        final EditText taskEditText = new EditText(this);
        taskEditText.setText(taskContent);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.modificar_tarea)
                .setMessage(getString(R.string.cambiar) + taskContent + getString(R.string.por))
                .setView(taskEditText)
                .setPositiveButton(R.string.cambiar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        int position = taskList.indexOf(taskContent);
                        String myTask = taskEditText.getText().toString();

                        //Modificar registro en la bbdd
                        if(myTask.isEmpty()){
                            //Warning para no dejar la tarea editada en blanco
                            toastWarning(getString(R.string.tarea_blanco));

                        }else {
                            db.collection("Tasks").document(taskIdList.get(position)).update("taskName", myTask);
                            //Toast de confirmación de modificación
                            toastOk(getString(R.string.tarea_modificada));

                        }

                    }
                })
                .setNegativeButton(getString(R.string.cancelar), null)
                .create();
        dialog.show();
    }

    //Método para poder borrar la tarea ya realizada
    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTv = parent.findViewById(R.id.item);
        String taskContent = taskTv.getText().toString();
        int position = taskList.indexOf(taskContent);

        db.collection("Tasks").document(taskIdList.get(position)).delete();

        toastOk(getString(R.string.tarea_realizada));

    }

    //Método para incluir un toast personalizado de confirmación.
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

    //Método para incluir un toast personalizado de advertencia.
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

    //Método para cerrar sesión en google con el botón "atras"
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mGoogleSignInClient.signOut();
        Intent intent = new Intent(MainActivity.this,Login.class);
        startActivity(intent);
    }

}