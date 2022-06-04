package com.snu.muc.dogeeye.common;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.provider.DocumentsContract;

public class PhotoStamp {
    int alpha = 50;
    int color = Color.WHITE;
    Location loc;
    int size = 25;


    public Bitmap stamp(Bitmap src, String customString){
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());

        // print the photo
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setTextSize(size);
        paint.setAntiAlias(true);

        // put letters

        Rect textBounds = new Rect();
        paint.getTextBounds(customString, 0, customString.length(), textBounds);
        canvas.drawText(customString, -textBounds.left, -textBounds.top, paint);
        canvas.drawText(loc.toString(), -textBounds.left, -2 * textBounds.top, paint );

        return result;
    }



}
