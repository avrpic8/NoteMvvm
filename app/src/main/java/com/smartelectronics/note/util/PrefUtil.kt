package com.smartelectronics.note.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PrefUtil {

    companion object{


        /**
         * Called to save supplied value in shared preferences against given key.
         * @param context Context of caller activity
         * @param key Key of value to save against
         * @param value Value to save
         */
        fun saveToPrefs(context: Context, key: String, value: String){

            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = prefs.edit()
            editor.putString(key, value)
            editor.commit()
        }

        fun saveToPrefs(context: Context, key: String, value: Boolean) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = prefs.edit()
            editor.putBoolean(key, value)
            editor.commit()
        }

        /**
         * Called to retrieve required value from shared preferences, identified by given key.
         * Default value will be returned of no value found or error occurred.
         * @param context Context of caller activity
         * @param key Key to find value against
         * @param defaultValue Value to return if no data found against given key
         * @return Return the value found against given key, default if not found or any error occurs
         */
        fun getFromPrefs(context: Context, key: String, defaultValue: String): String? {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            return try {
                sharedPrefs.getString(key, defaultValue)
            } catch (e: Exception) {
                e.printStackTrace()
                defaultValue
            }

        }

        fun getFromPrefs(context: Context, key: String, defaultValue: Boolean): Boolean {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            return try {
                sharedPrefs.getBoolean(key, defaultValue)
            } catch (e: Exception) {
                e.printStackTrace()
                defaultValue
            }

        }

    }
}