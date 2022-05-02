package com.snu.muc.dogeeye.ui.photo;

import android.app.Activity;

import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.ops.NormalizeOp;

import java.io.IOException;

public class ResnetRnnImageCaptioner extends ImageCaptioner {
    private static final float IMAGE_MEAN = 127.0f;
    private static final float IMAGE_STDDEV = 128.0f;

    public ResnetRnnImageCaptioner(Activity activity, ImageCaptioner.Device device, int numThreads)
            throws IOException {
        super(activity, device, numThreads);
    }

    @Override
    protected String getModelPath() {
        return "resnet-rnn.tflite";
    }

    @Override
    protected TensorOperator getPreprocessNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STDDEV);
    }
}
