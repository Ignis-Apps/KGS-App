package de.kgs.vertretungsplan.coverPlan;

import android.content.Context;

import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

class JsonDataStorage {

    void writeJSONToFile(Context c,JSONObject jo,String filename) throws Exception  {

        File file = new File(c.getFilesDir(),"json_data");

        if(!file.exists()) {
            if (!file.mkdir()) {
                throw new Exception("Directory could not be created" + file.getAbsolutePath());
            }
        }

        try{
            File f = new File(file, filename);
            FileWriter writer = new FileWriter(f);
            writer.append(jo.toString());
            writer.flush();
            writer.close();
            System.out.println("JSON successfully written to : " + f.getAbsolutePath());

        } catch (IOException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }

    }

    JSONObject readJSONFromFile(Context c, String filename) {

        File file = new File(c.getFilesDir(),"json_data");
        File f = new File(file,filename);
        String line;
        StringBuilder output = new StringBuilder();

        JSONObject j = null;

        try{
            BufferedReader reader = new BufferedReader(new FileReader(f));

            while((line = reader.readLine())!=null){
                output.append(line);
            }
            reader.close();

            j = new JSONObject(output.toString());
            System.out.println("JSON successfully read from : " + f.getAbsolutePath());
        }catch (FileNotFoundException fe){
            Crashlytics.logException(fe);;
            fe.printStackTrace();
        }catch (IOException ie){
            Crashlytics.logException(ie);
            ie.printStackTrace();
        }catch (JSONException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }

        return j;
    }


}
