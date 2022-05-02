package com.snu.muc.dogeeye.ui.photo;

import android.app.Activity;

import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.ops.NormalizeOp;

import java.io.IOException;

public class MobilenetGruImageCaptioner extends ImageCaptioner {
    private static final float IMAGE_MEAN = 127.0f;
    private static final float IMAGE_STDDEV = 128.0f;

    public MobilenetGruImageCaptioner(Activity activity, Device device, int numThreads)
        throws IOException {
        super(activity, device, numThreads);
    }

    @Override
    protected String getModelPath() {
        return "mobilenet-gru.tflite";
    }

    @Override
    protected TensorOperator getPreprocessNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STDDEV);
    }
}
