package com.mobdeve.chuachingdytocstamaria.mco.pomopet.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.models.Streak

class StreakDB(context: Context) {
    companion object{
        const val TABLE_NAME = "streak"
        const val COL_ID = "id"
        const val COL_DATE = "date"
        const val COL_CYCLE_NUM = "cycle_num"

        const val CREATE_TABLE_STATEMENT = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY,
                $COL_DATE TEXT NOT NULL,
                $COL_CYCLE_NUM INTEGER
            );
        """
    }

    private var dbHelper : DBHelper

    // Initializes the databaseHandler instance using the context provided.
    init {
        this.dbHelper = DBHelper(context)
    }


    fun addDate(streak: Streak): Int{
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_DATE, streak.date)

        val _id = db.insert(TABLE_NAME, null, contentValues)

        db.close()

        return _id.toInt()
    }

    fun updateCycle(streak: Streak) {
        val db = dbHelper.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(COL_CYCLE_NUM, streak.cycle_num)

        db.update(
            StreakDB.TABLE_NAME,
            contentValues,
            "${StreakDB.COL_DATE} = ?",
            arrayOf(streak.date)
        )

        db.close()
    }

    fun getAllDates(): ArrayList<Streak>{
        val streaks = ArrayList<Streak>()
        val db = dbHelper.readableDatabase
        var cursor: Cursor = db.query(TABLE_NAME, null, null, null,null, null, null)
        while(cursor.moveToNext()){
            val streak = Streak(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_CYCLE_NUM))
            )
            Log.d("getStreaks", "${streak.id}, ${streak.date}, ${streak.cycle_num}")
            streaks.add(streak)
        }
        Log.d("getStreaks", "${streaks.size}")

        return if (streaks.size > 0) streaks else arrayListOf(Streak())
    }

    fun getStreakDates(): Int{
        var total = 0

        val db = dbHelper.readableDatabase
        var cursor: Cursor = db.query(TABLE_NAME, null, null, null,null, null, null)
        while(cursor.moveToNext()){
            val streak = Streak(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_CYCLE_NUM))
            )
            Log.d("getStreaks", "${streak.id}, ${streak.date}, ${streak.cycle_num}")
            total++
        }

        return total
    }

    fun getCycles(): Int{
        var total = 0

        val db = dbHelper.readableDatabase
        var cursor: Cursor = db.query(TABLE_NAME, null, null, null,null, null, null)
        while(cursor.moveToNext()){
            val streak = Streak(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_CYCLE_NUM))
            )
            Log.d("getCycleNum", "${streak.id}, ${streak.date}, ${streak.cycle_num}")
            total += streak.cycle_num
        }

        return total
    }

    fun isDateInDB(date: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COL_DATE = ?",
            arrayOf(date),
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getStreakForDate(date: String): Streak? {
        val db = dbHelper.readableDatabase
        val selection = "$COL_DATE = ?"
        val selectionArgs = arrayOf(date)

        val cursor = db.query(
            TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var streak: Streak? = null

        if (cursor.moveToFirst()) {
            // Streak exists for the given date, create a Streak object
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
            val cycle = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CYCLE_NUM))
            streak = Streak(id, date, cycle)
        }

        cursor.close()
        return streak
    }
}