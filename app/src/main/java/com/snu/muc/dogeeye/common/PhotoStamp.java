package com.snu.muc.dogeeye.common;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.provider.DocumentsContract;

import com.snu.muc.dogeeye.model.Project;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.util.Random;

public class PhotoStamp {
    int alpha = 50;
    int color = Color.WHITE;
    int size = 25;

    String[] random_stamps = new String[] {
            "There was nowhere to go but everywhere",
            "How far can I go?",
            "The longest journey begins with a single step.",
            "Jobs fill your pockets, but adventures fill your soul",
            "Yeah, I also take photos"
    };


    public Bitmap stamp(Bitmap src, float totalDistance){
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());

        String customString = random_stamps[new Random().nextInt(random_stamps.length)];

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
        canvas.drawText(totalDistance + "KM", -textBounds.left, -2 * textBounds.top, paint );

        return result;
    }

    public void stamp(Bitmap image, Project current) {
        Canvas canvas = new Canvas(image);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        DateTime start = null;
        try {
            start = new DateTime(dateFormat.parse(current.getStartTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Paint paintForText = new Paint();
        paintForText.setColor(Color.BLACK);
        paintForText.setTextSize(30);
        paintForText.setTextAlign(Paint.Align.CENTER);

        String customString = random_stamps[new Random().nextInt(random_stamps.length)];
        canvas.drawText(customString,500.0f,50.0f, paintForText);

        paintForText.setTextSize(50);

        canvas.drawText(String.format("%d Steps", (int) current.getTotalStep()), 200.0f, 950.0f, paintForText); // Step
        canvas.drawText(String.format("%s M", current.getEveryMovingDistance()), 500.0f, 950.0f, paintForText); // Distance
        canvas.drawText(String.format("%d/%d", start.getMonthOfYear(), start.getDayOfMonth()), 800.0f, 950.0f, paintForText); // Date
//        canvas.drawText(String.format("%s", current.getStartTime()), 800.0f, 950.0f, paintForText); // Time
    }



}
