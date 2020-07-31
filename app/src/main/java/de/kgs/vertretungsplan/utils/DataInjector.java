package de.kgs.vertretungsplan.utils;

import android.content.Context;
import android.content.res.Resources;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import de.kgs.vertretungsplan.R;

public class DataInjector {

    public static void inject(Context c) {
        getData(c, "coverPlanToday.json", R.raw.today);
        getData(c, "coverPlanTomorrow.json", R.raw.tomorow);
    }

    private static void getData(Context c, String filename, int resId) {
        File file = new File(c.getFilesDir(), "json_data");
        if (file.exists() || file.mkdir()) {
            try {
                File f = new File(file, filename);
                FileWriter writer = new FileWriter(f);
                writer.append(loadStringFromRawResource(c.getResources(), resId));
                writer.flush();
                writer.close();
                PrintStream printStream = System.out;
                String sb = "JSON successfully written to : " + f.getAbsolutePath();
                printStream.println(sb);
            } catch (IOException e) {
                Crashlytics.logException(e);
                e.printStackTrace();
            }
        }
    }

    private static String loadStringFromRawResource(Resources resources, int resId) {
        InputStream rawResource = resources.openRawResource(resId);
        String content = streamToString(rawResource);
        try {
            rawResource.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    private static String streamToString(InputStream in) {
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder s = new StringBuilder();
        while (true) {
            try {
                String readLine = r.readLine();

                if (readLine == null) {
                    break;
                }
                String sb = readLine + "\n";
                s.append(sb);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return s.toString();
    }
}
