package com.snu.muc.dogeeye.ui.photo;

import static java.lang.Math.min;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.nnapi.NnApiDelegate;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ImageCaptioner {
    // To be singleton
    private static ImageCaptioner imageCaptioner = null;

    public static final String TAG = "ImageCaptionerWithSupport";
    private static String[] WORD_MAP;

    public enum Model {
        RESNET_RNN,
        MOBILENET_GRU
    }

    public enum Device {
        CPU,
        GPU,
        NNAPI
    }

    private final int imageSizeX;
    private final int imageSizeY;

    private GpuDelegate gpuDelegate = null;
    private NnApiDelegate nnApiDelegate = null;

    protected Interpreter tflite;

    private final Interpreter.Options tfliteOptions = new Interpreter.Options();

    private TensorImage inputImageBuffer;
    private int[][] outputCaptionIds = new int[22][1];
    Map<Integer, Object> outputBuffers = new HashMap<>();

    public static ImageCaptioner getInstance(Activity activity, Model model, Device device, int numThreads)
            throws IOException {
        if (imageCaptioner == null) {
            imageCaptioner = ImageCaptioner.create(activity, model, device, numThreads);
        }
        return imageCaptioner;
    }

    public static ImageCaptioner create(Activity activity, Model model, Device device, int numThreads)
            throws IOException {
        // Load word map file
        InputStream is = activity.getAssets().open("idmap");
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuilder total = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
        } catch (IOException e) {
             Log.d(TAG, "Cannot read word map file!");
        }
        WORD_MAP = total.toString().split("\n");

        if (model == Model.RESNET_RNN) {
            return new ResnetRnnImageCaptioner(activity, device, numThreads);
        } else if (model == Model.MOBILENET_GRU) {
            return new MobilenetGruImageCaptioner(activity, device, numThreads);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    protected ImageCaptioner(Activity activity, Device device, int numThreads) throws IOException {
        MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(activity, getModelPath());
        if (device == Device.NNAPI) {
            nnApiDelegate = new NnApiDelegate();
            tfliteOptions.addDelegate(nnApiDelegate);
        } else if (device == Device.GPU) {
            CompatibilityList compatList = new CompatibilityList();
            if (compatList.isDelegateSupportedOnThisDevice()) {
                GpuDelegate.Options delegateOptions = compatList.getBestOptionsForThisDevice();
                GpuDelegate gpuDelegate = new GpuDelegate(delegateOptions);
                tfliteOptions.addDelegate(gpuDelegate);
            } else {
                tfliteOptions.setUseXNNPACK(true);
            }
        } else if (device == Device.CPU) {
            tfliteOptions.setUseXNNPACK(true);
        }

        tfliteOptions.setNumThreads(numThreads);
        tflite = new Interpreter(tfliteModel, tfliteOptions);

        int imageTensorIndex = 0;
        Tensor imageTensor = tflite.getInputTensor(imageTensorIndex);
        int[] imageShape = imageTensor.shape();
        imageSizeX = imageShape[1]; // 224
        imageSizeY = imageShape[2]; // 224
        DataType imageDataType = imageTensor.dataType();
        inputImageBuffer = new TensorImage(imageDataType);

        for (int captionTensorIndex = 0; captionTensorIndex < 22; captionTensorIndex++) {
            outputBuffers.put(captionTensorIndex, outputCaptionIds[captionTensorIndex]);
        }
    }

    protected abstract String getModelPath();

    public String captionImage(final Bitmap bitmap, int sensorOrientation) {
        long startTimeForLoadImage = SystemClock.uptimeMillis();
        inputImageBuffer = loadImage(bitmap, sensorOrientation);
        long endTimeForLoadImage = SystemClock.uptimeMillis();

        long startTimeForInference = SystemClock.uptimeMillis();
        ByteBuffer[] inputBuffers = { inputImageBuffer.getBuffer() };
        tflite.runForMultipleInputsOutputs(inputBuffers, outputBuffers);
        long endTimeForInference = SystemClock.uptimeMillis();

        String resultString = "";
        for (int i = 0; i < 22; i++) {
            String word = WORD_MAP[outputCaptionIds[i][0]];
            if (word.equals("</S>")) {
                break;
            }
            resultString += word + " ";
        }
        return resultString;
    }

    public void close() {
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
        if (gpuDelegate != null) {
            gpuDelegate.close();
            gpuDelegate = null;
        }
        if (nnApiDelegate != null) {
            nnApiDelegate.close();
            nnApiDelegate = null;
        }
    }

    public int getImageSizeX() {
        return imageSizeX;
    }

    public int getImageSizeY() {
        return imageSizeY;
    }

    private TensorImage loadImage(final Bitmap bitmap, int sensorOrientation) {
        inputImageBuffer.load(bitmap);

        int cropSize = min(bitmap.getWidth(), bitmap.getHeight());
        int numRotation = sensorOrientation / 90;

        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                .add(new ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(new Rot90Op(numRotation))
                .add(getPreprocessNormalizeOp())
                .build();
        return imageProcessor.process(inputImageBuffer);
    }

    protected abstract TensorOperator getPreprocessNormalizeOp();
}
