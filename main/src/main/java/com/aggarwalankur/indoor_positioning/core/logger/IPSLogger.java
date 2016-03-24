package com.aggarwalankur.indoor_positioning.core.logger;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class IPSLogger extends Thread {
    /** Log Tag */
    private static final String TAG = "FTA_FTALogger";
    public void run() {
        String state = Environment.getExternalStorageState();
		/* This is what we want */
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardPath = new File("/sdcard/Log");
            try {
                if (!sdcardPath.exists())
                {
                    sdcardPath.mkdirs();
                }

                if (sdcardPath.canWrite()) {
                    try {
						/* Lets clear the old logs */
                        Runtime.getRuntime().exec("/system/bin/logcat -c");
                    } catch (IOException e1) {
						/*
						 * We could not clear the old logs. Thats not a big
						 * deal though.
						 */
                        Log.i(TAG, "Could not clear old logs");
                    }

                    try {
						/* Lets remove the old file */
                        Runtime
                                .getRuntime()
                                .exec("/system/bin/mv /sdcard/Log/IPS_Log.txt /sdcard/Log/IPS_Log.old.txt");
                    } catch (IOException e2) {
						/*
						 * We could not delete the old logs. Thats also not
						 * a big deal.
						 */
                        Log.i(TAG, "Could not delete old logs");
                    }

                    try {
						/*
						 * We are starting a new process to write logs to
						 * the SDCard in a single file with a maximum file
						 * size of 5MB and the path as mentioned.
						 */
                        Process p = Runtime
                                .getRuntime()
                                .exec(
                                        "/system/bin/logcat -v time -n 0 -f /sdcard/Log/IPS_Log.txt");

                        Field pidField;
                        try {
                            pidField = p.getClass().getDeclaredField("pid");
                            pidField.setAccessible(true);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e3) {
						/*
						 * We have a problem. There is no way out so lets
						 * just atleast print the stacktrace for someone who
						 * tries to see logcat output.
						 */
                        Log.e(TAG,  "Could not start the process to collect logs");
                        e3.printStackTrace();
                    }
                }
            } catch (SecurityException s1) {
                Log.e(TAG, "Security Exception on accessing SD CARD");
                s1.printStackTrace();
            }
        } else {
			/* Bad day, SD card is not available */
            Log.i(TAG,"Problem with SDCard, we are not going to create any logs");
        }
    }
}
