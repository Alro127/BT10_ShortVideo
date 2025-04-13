package com.example.bt10_shortvideo.utils;

import static com.example.bt10_shortvideo.LoginActivity.userEmail;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.example.bt10_shortvideo.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
    public static File getFile(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        File file = new File(context.getCacheDir(), "temp_video");
        FileOutputStream outputStream = new FileOutputStream(file);

        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            outputStream.write(buf, 0, len);
        }

        outputStream.close();
        inputStream.close();

        return file;
    }
}

