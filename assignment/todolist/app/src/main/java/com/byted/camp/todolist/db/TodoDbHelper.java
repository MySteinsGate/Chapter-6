package com.byted.camp.todolist.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.byted.camp.todolist.db.TodoContract.SQL_CREATE_ENTRIES;
import static com.byted.camp.todolist.db.TodoContract.TodoEntry.TABLE_NAME;

/**
 * Created on 2019/1/22.
 *
 * @author xuyingyi@bytedance.com (Yingyi Xu)
 */
public class TodoDbHelper extends SQLiteOpenHelper {

    // TODO 定义数据库名、版本；创建数据库
    private static int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "Todo.db";

    //单例模式
    private static volatile TodoDbHelper instance = null;

    private TodoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //单例模式
    public static TodoDbHelper getDBHelper(Context context) {
        //version标识版本
        if (instance == null) {
            synchronized (TodoDbHelper.class) {
                if (instance == null)
                    instance = new TodoDbHelper(context);
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        //若用户直接安装
        String SQL_ADD_PRIORITY =
                "ALTER TABLE " + TABLE_NAME + " ADD COLUMN priority INTEGER default 0";
        db.execSQL(SQL_ADD_PRIORITY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //若用户是从第一版升级覆盖
        if (oldVersion == 1 && newVersion == 2) {   //升级数据库版本
            //新增优先级字段
            String SQL_ADD_PRIORITY =
                    "ALTER TABLE " + TABLE_NAME + " ADD COLUMN priority INTEGER default 0";
            db.execSQL(SQL_ADD_PRIORITY);
        }
    }
}
