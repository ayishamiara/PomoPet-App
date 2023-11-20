package com.mobdeve.chuachingdytocstamaria.mco.pomopet.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.models.ToDo

class ToDoDB(context: Context) {
    companion object{
        const val TABLE_NAME = "todo"
        const val COL_IS_DONE = "isDone"
        const val COL_LABEL = "label"
        const val COL_ID = "id"

        const val CREATE_TABLE_STATEMENT = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY,
                $COL_IS_DONE INTEGER NOT NULL,
                $COL_LABEL TEXT NOT NULL
            );
        """
    }

    private var dbHelper : DBHelper

    // Initializes the databaseHandler instance using the context provided.
    init {
        this.dbHelper = DBHelper(context)
    }


    fun addToDo(todo: ToDo): Int{
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_LABEL, todo.label)
        contentValues.put(COL_IS_DONE, todo.isDone)

        val _id = db.insert(TABLE_NAME, null, contentValues)

        db.close()

        return _id.toInt()
    }

    fun updateTodo(todo: ToDo){
        val db = dbHelper.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(COL_LABEL, todo.label)
        contentValues.put(COL_IS_DONE, todo.isDone)

        db.update(TABLE_NAME, contentValues, "$COL_ID = ?", arrayOf(todo.id.toString()))

        db.close()
    }

    fun getAllTodos(): ArrayList<ToDo>{
        val todos = ArrayList<ToDo>()
        val db = dbHelper.readableDatabase
        var cursor: Cursor = db.query(TABLE_NAME, null,
            "$COL_IS_DONE=?", arrayOf("0"),
            null, null, null)
        while(cursor.moveToNext()){
            val todo = ToDo(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_LABEL))
            )
            Log.d("gettodos", "${todo.id}, ${todo.label}")
            todos.add(todo)
        }
        Log.d("gettodos", "${todos.size}")

        return if (todos.size > 0) todos else arrayListOf(ToDo())

    }



}