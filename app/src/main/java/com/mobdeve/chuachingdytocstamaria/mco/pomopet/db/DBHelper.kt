package com.mobdeve.chuachingdytocstamaria.mco.pomopet.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "pomopet_db.db"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(ToDoDB.CREATE_TABLE_STATEMENT)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS ${ToDoDB.TABLE_NAME}")
        onCreate(db)
    }
}