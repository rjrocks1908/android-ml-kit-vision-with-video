package com.google.mlkit.vision.demo.java.posedetector;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.demo.video.VideoBaseActivity;
import com.google.mlkit.vision.pose.Pose;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

public class CaptureFramesForTest {

    private final Context context;
    private final Uri uri, normUri;
    private final ContentResolver resolver, normResolver;

    public CaptureFramesForTest(Context context, Uri uri, ContentResolver resolver, Uri normUri, ContentResolver normResolver) {
        this.context = context;
        this.uri = uri;
        this.resolver = resolver;
        this.normUri = normUri;
        this.normResolver = normResolver;
    }

    private boolean isExternalStorageWritable() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.e("State", "Yes Writable");
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPermission(String permission) {
        int check = ContextCompat.checkSelfPermission(context, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    public void saveToExternal(String s, int fileCount) {
        // Captures the frames by reps and store it in external storage
        if (isExternalStorageWritable() && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.e("State2", "Location: " + Environment.getExternalStorageDirectory().getPath());
            File file;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Log.e("CHECK", "R folder");
                file = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "frames" + fileCount + ".txt" );
            } else {
                Log.e("CHECK", "Q folder");
                file = new File(Environment.getExternalStorageDirectory(), "frames" + fileCount + ".txt");
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                fos.write(s.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                        Log.e("State1", "File Saved" + Environment.getExternalStorageDirectory().getPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void saveToExternal2(String s, String name) {
        // Captures the frames of whole session and store it in internal storage
        if (isExternalStorageWritable() && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            File myDir;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                myDir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/FitBuddy");
//            } else {
//                myDir = new File(Environment.getExternalStorageDirectory() + "/FitBuddy");
//            }
//            if (!myDir.exists()){
//                boolean success = myDir.mkdirs();
//                if (!success){
//                    Toast.makeText(context, "Error while creating directory", Toast.LENGTH_SHORT).show();
//                }
//            }
            File file = null;
            if (VideoBaseActivity.chosenFileName != null) {
//                 file = new File(myDir, VideoBaseActivity.chosenFileName + "_" + name + "framesFull.txt");
            }


            FileOutputStream fos = null;
            OutputStream os = null, normOs = null;
            try {
//                fos = new FileOutputStream(file, true);
//                fos.write(s.getBytes());
                if (!name.equals("norm")) {
                    os = resolver.openOutputStream(uri, "wa");
                    os.write(s.getBytes());
                }else {
                    normOs = normResolver.openOutputStream(normUri, "wa");
                    normOs.write(s.getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (/*fos != null &&*/ os != null) {
                    try {
//                        fos.close();
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (/*fos != null &&*/ normOs != null) {
                    try {
//                        fos.close();
                        normOs.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void save(String s, int fileCount) {
        // Captures the frames by reps and store it in internal storage
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput("frames" + fileCount + ".txt", Context.MODE_APPEND | Context.MODE_PRIVATE);
//            fos = context.openFileOutput("frames.txt", Context.MODE_APPEND | Context.MODE_PRIVATE);
            fos.write(s.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void captureFullSessionFrames(List<PointF3D> normalizedLandmarks, Pose pose, String name){
        StringBuilder temp = new StringBuilder();
        for (int i=0; i<normalizedLandmarks.size(); i++){
            if (i<normalizedLandmarks.size()-1) {
                temp.append(normalizedLandmarks.get(i).getX())
                        .append(",")
                        .append(normalizedLandmarks.get(i).getY())
                        .append(",")
                        .append(normalizedLandmarks.get(i).getZ())
                        .append(",")
                        .append(Objects.requireNonNull(pose.getPoseLandmark(i)).getInFrameLikelihood())
                        .append(",");
            }else {
                temp.append(normalizedLandmarks.get(i).getX())
                        .append(",")
                        .append(normalizedLandmarks.get(i).getY())
                        .append(",")
                        .append(normalizedLandmarks.get(i).getZ())
                        .append(",")
                        .append(Objects.requireNonNull(pose.getPoseLandmark(i)).getInFrameLikelihood())
                        .append("#");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveToExternal2(temp.toString(), name);
                }
            }
        }
    }

    public void save2(String s) {
        // Captures the frames of whole session and store it in internal storage
        FileOutputStream fos = null;
        try {
//            fos = context.openFileOutput("frames" + fileCount + ".txt", Context.MODE_APPEND | Context.MODE_PRIVATE);
            fos = context.openFileOutput("framesFull.txt", Context.MODE_APPEND | Context.MODE_PRIVATE);
            fos.write(s.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
