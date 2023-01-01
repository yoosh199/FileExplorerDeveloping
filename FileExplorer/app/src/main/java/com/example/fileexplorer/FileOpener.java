package com.example.fileexplorer;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileOpener {

    private final static String TAG = "LOG";
    public static void openFile(Context context, File file){
        Uri uri = FileProvider.getUriForFile(context.getApplicationContext(), context.getApplicationContext().getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if(uri.toString().contains(".doc")){
            intent.setDataAndType(uri, "application/msword");
        }
        else if(uri.toString().contains(".pdf")){
            intent.setDataAndType(uri, "application/pdf");
    }
        else if(uri.toString().contains(".mp3")||uri.toString().contains(".wav")){
            intent.setDataAndType(uri, "audio/x-wav");
        }
        else if(uri.toString().toLowerCase().contains(".jpeg") || uri.toString().toLowerCase().contains(".jpg") ||
                uri.toString().toLowerCase().contains(".png")){
            intent.setDataAndType(uri, "image/jpeg");
        }
        else if(uri.toString().toLowerCase().contains(".mp4")){
            intent.setDataAndType(uri, "video/*");
        }
        else{
            intent.setDataAndType(uri, "*/*");
        }



        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(context.openFileOutput("recentFile.txt", MODE_PRIVATE)));
            bw.write(file.getAbsolutePath());
            bw.close();
            Log.d(TAG, "openFile: ");
        } catch (IOException e) {
            e.printStackTrace();
        }


        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }
}
