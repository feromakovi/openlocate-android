package com.openlocate.android.core;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class SharedPreferenceUtils {

    private static SharedPreferenceUtils mSharedPreferenceUtils;
    protected Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;

    private SharedPreferenceUtils(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(Constants.OPENLOCATE, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPreferenceUtils getInstance(Context context) {

        if (mSharedPreferenceUtils == null) {
            mSharedPreferenceUtils = new SharedPreferenceUtils(context.getApplicationContext());
        }
        return mSharedPreferenceUtils;
    }

    public void setValue(String key, String value) {
        SharedPreferences.Editor mSharedPreferencesEditor = mSharedPreferences.edit();
        mSharedPreferencesEditor.putString(key, value);
        mSharedPreferencesEditor.commit();
    }

    public void setValue(String key, int value) {
        SharedPreferences.Editor mSharedPreferencesEditor = mSharedPreferences.edit();
        mSharedPreferencesEditor.putInt(key, value);
        mSharedPreferencesEditor.commit();
    }

    public void setValue(String key, double value) {
        setValue(key, Double.toString(value));
    }

    public void setValue(String key, long value) {
        SharedPreferences.Editor mSharedPreferencesEditor = mSharedPreferences.edit();
        mSharedPreferencesEditor.putLong(key, value);
        mSharedPreferencesEditor.commit();
    }

    public void setValue(String key, boolean value) {
        SharedPreferences.Editor mSharedPreferencesEditor = mSharedPreferences.edit();
        mSharedPreferencesEditor.putBoolean(key, value);
        mSharedPreferencesEditor.commit();
    }

    public String getStringValue(String key, String defaultValue) {
        return mSharedPreferences.getString(key, defaultValue);
    }

    public int getIntValue(String key, int defaultValue) {
        return mSharedPreferences.getInt(key, defaultValue);
    }

    public long getLongValue(String key, long defaultValue) {
        return mSharedPreferences.getLong(key, defaultValue);
    }

    public boolean getBoolanValue(String keyFlag, boolean defaultValue) {
        return mSharedPreferences.getBoolean(keyFlag, defaultValue);
    }

    public void removeKey(String key) {
        SharedPreferences.Editor mSharedPreferencesEditor = mSharedPreferences.edit();
        if (mSharedPreferencesEditor != null) {
            mSharedPreferencesEditor.remove(key);
            mSharedPreferencesEditor.commit();
        }
    }

    public void saveMap(String key, HashMap<String,String> inputMap){
        JSONObject jsonObject = new JSONObject(inputMap);
        String jsonString = jsonObject.toString();
        SharedPreferences.Editor mSharedPreferencesEditor = mSharedPreferences.edit();
        mSharedPreferencesEditor.putString(key, jsonString);
    }

    public HashMap<String,String> loadMap(String mapKey){
        HashMap<String,String> outputMap = new HashMap<>();
        String jsonString = mSharedPreferences.getString(mapKey, (new JSONObject()).toString());
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonString);
            Iterator<String> keysItr = jsonObject.keys();
            while(keysItr.hasNext()) {
                String key = keysItr.next();
                String value = jsonObject.getString(key);
                outputMap.put(key, value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return outputMap;
    }

    public void clear() {
        SharedPreferences.Editor mSharedPreferencesEditor = mSharedPreferences.edit();
        mSharedPreferencesEditor.clear().commit();
    }
}