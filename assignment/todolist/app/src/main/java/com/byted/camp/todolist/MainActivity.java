package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;


import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.operation.activity.DatabaseActivity;
import com.byted.camp.todolist.operation.activity.DebugActivity;
import com.byted.camp.todolist.operation.activity.SettingActivity;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;
    private TodoDbHelper dbHelper;//dbHelper 单例模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //利用静态方法获取dbHelper
        dbHelper = TodoDbHelper.getDBHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }
        });
        recyclerView.setAdapter(notesAdapter);

        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            case R.id.action_database:
                startActivity(new Intent(this, DatabaseActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {
        // TODO 从数据库中查询数据，并转换成 JavaBeans

        //note list
        List<Note> allNotes = new ArrayList<>();
        //只读
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //选择所有行所有列
        Cursor cursor = db.query(
                TodoContract.TodoEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            //ID
            long id = cursor.getLong(cursor
                    .getColumnIndexOrThrow(TodoContract.TodoEntry.COLUMN_NAME_ID));
            //DATE
            long dateMilli = cursor.getLong(cursor
                    .getColumnIndex(TodoContract.TodoEntry.COLUMN_NAME_DATE));
            Date date = new Date(dateMilli);
            //STATE
            int stateInt = cursor.getInt(cursor
                    .getColumnIndex(TodoContract.TodoEntry.COLUMN_NAME_STATE));
            State state = State.from(stateInt);
            //CONTENT
            String content = cursor.getString(cursor
                    .getColumnIndex(TodoContract.TodoEntry.COLUMN_NAME_CONTENT));
            //PRIORITY
            int priority = cursor.getInt(cursor
                    .getColumnIndex(TodoContract.TodoEntry.COLUMN_NAME_PRIORITY));

            //Note对象
            Note note = new Note(id);
            note.setDate(date);
            note.setState(state);
            note.setContent(content);
            note.setPriority(priority);

            //加入到list中
            allNotes.add(note);
        }
        cursor.close();
        //根据优先级排序
        Collections.sort(allNotes, new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
                int p1 = o1.getPriority();
                int p2 = o2.getPriority();
                long d1 = o1.getDate().getTime();
                long d2 = o2.getDate().getTime();

                if(p1 > p2 || (p1 == p2 && d1 < d2)){
                    return -1;
                }
                return 1;
            }
        });
        return allNotes;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //根据ID访问
        String selection = TodoContract.TodoEntry.COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(note.id)};
        //删除
        int deletedRows = db.delete(
                TodoContract.TodoEntry.TABLE_NAME,
                selection,
                selectionArgs
        );
        //刷新页面
        notesAdapter.refresh(loadNotesFromDatabase());
    }

    private void updateNode(Note note) {
        // TODO 更新数据
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //根据ID访问
        String selection = TodoContract.TodoEntry.COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(note.id)};
        //更新State
        ContentValues values = new ContentValues();
        values.put(TodoContract.TodoEntry.COLUMN_NAME_STATE, note.getState().intValue);

        int count = db.update(
                TodoContract.TodoEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
        //刷新页面
        notesAdapter.refresh(loadNotesFromDatabase());
    }
}
