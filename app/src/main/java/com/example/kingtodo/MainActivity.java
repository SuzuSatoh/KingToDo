package com.example.kingtodo;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kingtodo.Adapter.ToDoAdapter;
import com.example.kingtodo.Model.ToDoModel;
import com.example.kingtodo.Utils.DatabaseHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements DialogCloseListener {

    private DatabaseHandler db;

    private RecyclerView tasksRecyclerView;
    private ToDoAdapter tasksAdapter;
    private FloatingActionButton fab;
    private Button btn;
    private List<ToDoModel> taskList;

    private ProgressBar progressBar;
    private int progressValue = 0;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        db = new DatabaseHandler(this);
        db.openDatabase();

        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        tasksAdapter = new ToDoAdapter(db.getAllTasks(), db, this, new ToDoAdapter.OnItemCheckedChangeListener() {
            @Override
            public void onItemCheckedChange(boolean isChecked) {
                if (isChecked) {
                    progressValue += 1;
                } else {
                    progressValue -= 1;
                }
                progressBar.setProgress(progressValue);
                saveProgressValue(progressValue);
            }
        });
        tasksRecyclerView.setAdapter(tasksAdapter);

        RecyclerItemTouchHelper itemTouchHelper = new RecyclerItemTouchHelper(tasksAdapter);
        new ItemTouchHelper(itemTouchHelper).attachToRecyclerView(tasksRecyclerView);

        fab = findViewById(R.id.fab);

        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG);
            }
        });

        progressBar = findViewById(R.id.progressBar2);
        progressBar.setMax(100);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        progressValue = getSavedProgressValue();
        progressBar.setProgress(progressValue);

        progressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (progressValue < 100) {
                    progressValue++;
                }
                progressBar.setProgress(progressValue);
                saveProgressValue(progressValue);
            }
        });
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);
        tasksAdapter.notifyDataSetChanged();

        if (progressValue < 100) {
            progressValue += 1;
        }
        progressBar.setProgress(progressValue);
        saveProgressValue(progressValue);
    }

    private void saveProgressValue(int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("progressValue", value);
        editor.apply();
    }

    private int getSavedProgressValue() {
        return sharedPreferences.getInt("progressValue", 0);
    }
}
