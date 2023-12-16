package com.example.dailyselfie.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.appcompat.app.AppCompatActivity;

public class bitmapEdit extends AppCompatActivity {

    //Tùy chỉnh bitmap
    public static Bitmap setImageFromFilePath(String imagePath, int targetW, int targetH) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
        bmpOptions.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imagePath, bmpOptions);
        int photoW = bmpOptions.outWidth;
        int photoH = bmpOptions.outHeight;
        int scaleFactor = Math.max(photoW / targetW, photoH / targetH);
        bmpOptions.inJustDecodeBounds = false;
        bmpOptions.inSampleSize = scaleFactor;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmpOptions);
        return bitmap;
    }
    //Tùy chỉnh theo cố định
    public static Bitmap setImageFromFilePath(String imagePath) {
        return setImageFromFilePath(imagePath, 200, 200);
    }
}