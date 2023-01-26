package com.example.cutenotes;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
                        .setTitle("New Task")
                        .setMessage("Task:")
                        .setView(taskEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Añadir tarea a la base de datos

                                String myTask = taskEditText.getText().toString();
                                Map<String, Object> task = new HashMap<>();
                                task.put("taskName", myTask);
                                task.put("emailUser", emailUser);

                                //Añadir registro en la bbdd
                                db.collection("Tasks").add(task);
                            }
                        })
                        .setNegativeButton("Cancel", null)
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

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Modify Task")
                .setMessage("Cambiar \"" + taskContent + "\" por: ")
                .setView(taskEditText)
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Modificar tarea a la base de datos
                        int position = taskList.indexOf(taskContent);
                        String myTask = taskEditText.getText().toString();

                        //Añadir registro en la bbdd
                        db.collection("Tasks").document(taskIdList.get(position)).update("taskName", myTask);

                        Toast toast = new Toast(getApplicationContext());
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.toast,
                                findViewById(R.id.lytLayout));
                        TextView txtMsg = layout.findViewById(R.id.txtMensaje);
                        txtMsg.setText(R.string.Update);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setView(layout);
                        toast.show();

                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTv = parent.findViewById(R.id.item);
        String taskContent = taskTv.getText().toString();
        int position = taskList.indexOf(taskContent);
        db.collection("Tasks").document(taskIdList.get(position)).delete();
        Toast toast = new Toast(getApplicationContext());
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast,
                findViewById(R.id.lytLayout));
        TextView txtMsg = layout.findViewById(R.id.txtMensaje);
        txtMsg.setText(R.string.delete);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}