package com.mobdeve.chuachingdytocstamaria.mco.pomopet.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "pomopet_db.db"
    }

    // Create the database tables
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(ToDoDB.CREATE_TABLE_STATEMENT)
        db?.execSQL(StreakDB.CREATE_TABLE_STATEMENT)
    }

    // Upgrade the database if necessary
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS ${ToDoDB.TABLE_NAME}")
        db?.execSQL("DROP TABLE IF EXISTS ${StreakDB.TABLE_NAME}")
        onCreate(db)
    }
}