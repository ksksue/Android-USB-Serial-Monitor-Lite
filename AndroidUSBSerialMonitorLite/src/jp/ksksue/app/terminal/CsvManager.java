package jp.ksksue.app.terminal;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

public class CsvManager {
    private static final boolean DEBUGSHOW_READWORDS    = false;
    private static final boolean DEBUGSHOW_WRITEWORDS   = false;

    private static final String TAG = "CsvManager";

    private FileInputStream mCsvFileInput;
    private BufferedReader mCsvFileBuf;

    /**
     * Open CSV File
     * @param fileName CSV File Name
     * @param ctx Activity Context
     * @return true : open succeeded, false : not open succeeded
     */
    public boolean open(String fileName,Context ctx) {
        try{
            mCsvFileInput = ctx.openFileInput(fileName);

            if(mCsvFileInput != null) {
                mCsvFileBuf = new BufferedReader(new InputStreamReader(mCsvFileInput));
                return true;
            } else {
                return false;
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG,e.toString());
        }
        return false;
    }

    /**
     * Copy CSV File to ArrayAdapter
     * @param Array ArrayAdapter<String>
     */
    public void copyCsvToArrayAdapter(ArrayAdapter<String> Array) {
        String line;

        if(mCsvFileBuf == null) {
            return;
        }

        if(Array == null) {
            return;
        }

        try {
            while((line = mCsvFileBuf.readLine()) != null) {
                if(DEBUGSHOW_READWORDS) {
                    Log.d(TAG, "R: "+line);
                }
                StringTokenizer st = new StringTokenizer(line, ",");

                while(st.hasMoreTokens()) {
                    Array.add(st.nextToken());
                }
            }
        } catch (IOException e) {
            Log.e(TAG,e.toString());
        }
    }

    /**
     * copy ArrayAdapter to CSV File
     * 
     * @param Array ArrayAdapter<String>
     * @param columnNum Column Number of CSV
     * @param ctx Activity Context
     */
    public void copyArrayAdapterToCsv(ArrayAdapter<String> Array, String fileName, int columnNum, Context ctx) {
        if(Array == null) {
            return;
        }
        if(columnNum == 0) {
            return;
        }
        
        try {
            FileOutputStream fileOutput = ctx.openFileOutput(fileName, Activity.MODE_PRIVATE);

            BufferedWriter bw 
              = new BufferedWriter(new OutputStreamWriter(fileOutput)); 

            String line;
            for(int i = 0; i < Array.getCount()/columnNum; i++) {
                line = "";
                for(int j=0; j<columnNum; j++) {
                    line += Array.getItem(i)+",";
                }
                bw.write(line);
                bw.newLine();
                if(DEBUGSHOW_WRITEWORDS) {
                    Log.d(TAG, "W: "+line);
                }
            }
            bw.close();
            fileOutput.close();

          } catch (FileNotFoundException e) {
              Log.e(TAG,e.toString());
          } catch (IOException e) {
              Log.e(TAG,e.toString());
          }
    }

    /**
     * close buffer
     */
    public void close() {
        if(mCsvFileBuf == null) {
            return;
        }
        try {
            mCsvFileBuf.close();
            mCsvFileInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
