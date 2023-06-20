package com.scan.imagetotext.ResultDatabaseHelper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.scan.imagetotext.Model.ScanResultModel

class ScanResultDatabaseHelper(private val context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, VERSTION
) {
    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TABLE_QUERY = ("CREATE TABLE "
                + TABLE_NAME + "("
                + COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + COL_2 + " TEXT ,"
                + COL_3 + " TEXT"
                + ")")
        db.execSQL(CREATE_TABLE_QUERY)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun insertToScanResult(dataModel: ScanResultModel) {
        val db = this.writableDatabase
        db.enableWriteAheadLogging()
        val values = ContentValues()
        values.put(COL_2, dataModel.resultData)
        values.put(COL_3, dataModel.fileName)
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    val allScanResult: List<ScanResultModel>
        get() {
            val modellist: MutableList<ScanResultModel> = ArrayList()
            val sql = "SELECT * FROM " + TABLE_NAME
            val db = this.readableDatabase
            val cursor = db.rawQuery(sql, null)
            if (cursor.moveToFirst()) {
                do {
                    var model = ScanResultModel()
                    model.id = cursor.getString(0).toInt()
                    model.resultData = cursor.getString(1)
                    model.fileName = cursor.getString(2)
                    modellist.add(model)
                } while (cursor.moveToNext())
            }
            return modellist
        }

    fun deleteAllScanResult() {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, null, null)

        /*
        db.delete(TABLE_NAME, COL_1 + " = ?",
                new String[]{String.valueOf(dataModel.getId())});*/db.close()
    }

    fun deleteScanResultById(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, COL_1 + " = ?", arrayOf(id.toString()))
        db.close()
    }

    companion object {
        var DATABASE_NAME = "Resultdb"
        var VERSTION = 1
        var TABLE_NAME = "ScanResultTable"
        var COL_1 = "id"
        var COL_2 = "ResultItem"
        var COL_3 = "FileName"

    }
}