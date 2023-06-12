package com.example.materials;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.text.format.DateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class FileManager {
    private Context _context;
    public FileManager(Context context){
        _context = context;
    }
    public void SaveToFile(SharedPreferences prefs) throws JSONException {
        Date d = new Date();
        CharSequence s  = DateFormat.format("MMMM d, yyyy ", d.getTime());
        JSONObject parent = new JSONObject();
        JSONObject obj = new JSONObject();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Map<String,?> keys = prefs.getAll();
            for(Map.Entry<String,?> entry : keys.entrySet()){
                obj.append(entry.getKey(),entry.getValue());
            }
            parent.append(s.toString(),obj);
        }
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(dir, "materials_history.json");

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.append(parent.toString());
        } catch (IOException e) {
            //Handle exception
        }
    }
    public String ReadFile(){

        return "History file content to return.";
    }
}
