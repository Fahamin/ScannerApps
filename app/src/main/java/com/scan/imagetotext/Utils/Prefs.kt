package com.scan.imagetotext.Utils

import android.content.Context
import android.content.SharedPreferences
import com.scan.imagetotext.R

class Prefs(context: Context) {
    private val sharedPreferences: SharedPreferences
    private val editor: SharedPreferences.Editor

    init {
        sharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    fun setInt(key: String?, value: Int) {
        editor.putInt(key, value)
        editor.apply()
    }

    fun setString(key: String?, value: String?) {
        editor.putString(key, value)
        editor.apply()
    }

    fun setBoolean(key: String?, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String?, def: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, def)
    }

    fun getInt(key: String?, def: Int): Int {
        return sharedPreferences.getInt(key, def)
    }

    fun getString(key: String?, def: String?): String? {
        return sharedPreferences.getString(key, def)
    }

    var premium: Int
        get() = sharedPreferences.getInt("Premium", 0)
        set(value) {
            editor.putInt("Premium", value)
            editor.apply()
        }
    var isRemoveAd: Boolean
        get() = getBoolean("isRemoveAd", false)
        set(value) {
            editor.putBoolean("isRemoveAd", value)
            editor.apply()
        }

    fun canDownload(): Boolean {
        return getBoolean("canDownload", false)
    }

    fun setCanDownload(value: Boolean) {
        editor.putBoolean("canDownload", value)
        editor.apply()
    }
}