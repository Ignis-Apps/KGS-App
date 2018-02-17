package de.kgs.vertretungsplan.coverPlan;

import android.content.Context;

import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

class JsonDataStorage {

    void writeJSONToFile(Context c,JSONObject jo,String filename)  {

        File file = new File(c.getFilesDir(),"json_data");

        if(!file.exists())
            file.mkdir();

        try{
            File f = new File(file, filename);
            FileWriter writer = new FileWriter(f);
            writer.append(jo.toString());
            writer.flush();
            writer.close();
            System.out.println("JSON successfully written to : " + f.getAbsolutePath());

        } catch (IOException e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
        }

    }

    JSONObject readJSONFromFile(Context c, String filename) {

        File file = new File(c.getFilesDir(),"json_data");
        File f = new File(file,filename);
        String line,output = "";

        try{
            BufferedReader reader = new BufferedReader(new FileReader(f));

            while((line = reader.readLine())!=null){
                output+=line;
            }
            reader.close();
        }catch (FileNotFoundException fe){
            FirebaseCrash.report(fe);
            fe.printStackTrace();
        }catch (IOException ie){
            FirebaseCrash.report(ie);
            ie.printStackTrace();
        }

        JSONObject j = null;
        try {
            j = new JSONObject(output);
            System.out.println("JSON successfully read from : " + f.getAbsolutePath());
        } catch (JSONException e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
        }

        return j;
    }


}
