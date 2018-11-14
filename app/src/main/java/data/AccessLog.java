package data;

import android.content.Context;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class AccessLog {
    public static final String RECORD_FILE_NAME = "record";

    public static void addRecord(Context context, String line) {
        if (context != null) {
            try {
                FileOutputStream fis = context.openFileOutput(RECORD_FILE_NAME, Context.MODE_PRIVATE | Context.MODE_APPEND);
                DataOutputStream dos = new DataOutputStream(fis);
                dos.write((line+"\n").getBytes());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static ArrayList<String> readRecords(Context context) {
        if (context != null) {
            try {
                FileInputStream fis = context.openFileInput(RECORD_FILE_NAME);
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String line = "";

                ArrayList<String> result = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    result.add(line);
                }
                return result;
            } catch (FileNotFoundException fex) {
                return null;
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
