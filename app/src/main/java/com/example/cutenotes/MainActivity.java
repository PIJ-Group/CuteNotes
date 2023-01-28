package com.example.cutenotes;

import static com.example.cutenotes.R.string.empty_modified_task;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newTask:
                //activar el cuadro de diàlogo para añadir tareas

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

            case R.id.logout:
                //cierre de sesión firebase
                nAuth.signOut();
                onBackPressed();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

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
                        //Modificar tarea a la base de datos
                        int position = taskList.indexOf(taskContent);
                        String myTask = taskEditText.getText().toString();

                        //Modificar registro en la bbdd
                        if(myTask.isEmpty()){

                            toastWarning(getString(R.string.tarea_blanco));

                        }else {
                            db.collection("Tasks").document(taskIdList.get(position)).update("taskName", myTask);

                            toastOk(getString(R.string.tarea_modificada));

                        }

                    }
                })
                .setNegativeButton(getString(R.string.cancelar), null)
                .create();
        dialog.show();
    }

    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTv = parent.findViewById(R.id.item);
        String taskContent = taskTv.getText().toString();
        int position = taskList.indexOf(taskContent);

        db.collection("Tasks").document(taskIdList.get(position)).delete();

        toastOk(getString(R.string.tarea_realizada));

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