package com.snu.muc.dogeeye.ui.photo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.snu.muc.dogeeye.R;

public class PhotoCaptureDialog extends Dialog {
    private Context context;
    public PhotoCaptureDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_capture_dialog);
    }
}
