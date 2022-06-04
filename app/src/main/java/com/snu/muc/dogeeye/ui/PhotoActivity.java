package com.snu.muc.dogeeye.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.common.util.concurrent.ListenableFuture;
import com.snu.muc.dogeeye.R;
import com.snu.muc.dogeeye.common.TextSpeechModule;
import com.snu.muc.dogeeye.ui.photo.ImageCaptioner;

import org.w3c.dom.Text;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class PhotoActivity extends AppCompatActivity {
    private final String TAG = "PhotoActivity";
    private static final int ANALYSIS_DELAY_MS = 3_000;
    private static final int INVALID_TIME = -1;
    private long lastAnalysisTime = INVALID_TIME;
    private TextSpeechModule ttsModule = TextSpeechModule.getInstance();
    private ImageCaptioner imageCaptioner;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private MutableLiveData<String> caption = new MutableLiveData<>();
    private int imageSizeX;
    private int imageSizeY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        // Bind caption text view
        bindText();

        // Prepare camera
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(getIntent().getIntExtra("facing", 1))
                .build();

        // Bind preview
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = bindPreview(cameraProvider, cameraSelector);
                if (getIntent().getIntExtra("facing", 1) == 1) {
                    bindCaptioning(cameraProvider, cameraSelector, preview);
                }
            } catch (ExecutionException | InterruptedException e) {
            }
        }, ContextCompat.getMainExecutor(this));

        // Get imageCaptioner object
        try {
            imageCaptioner = ImageCaptioner.getInstance(
                    this,
                    ImageCaptioner.Model.MOBILENET_GRU,
                    ImageCaptioner.Device.NNAPI,
                    4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void bindCaptioning(@NonNull ProcessCameraProvider cameraProvider,
                        @NonNull CameraSelector cameraSelector,
                        @NonNull Preview preview) {
        final ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .setTargetResolution(new Size(224, 224))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                final long now = SystemClock.uptimeMillis();
                // Drop frame if an image has been analyzed less than ANALYSIS_DELAY_MS ms ago
                Log.d(TAG, String.valueOf(ttsModule.isSpeaking()));
                if ((ttsModule.isSpeaking()) ||
                        lastAnalysisTime != INVALID_TIME &&
                        (now - lastAnalysisTime < ANALYSIS_DELAY_MS)) {
                    image.close();
                    return;
                }
                int rotationDegrees = image.getImageInfo().getRotationDegrees();
                ImageProxy.PlaneProxy plane = image.getPlanes()[0];
                ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
                int pixelStride = plane.getPixelStride();
                int rowStride = plane.getRowStride();
                int rowPadding = rowStride - pixelStride * image.getWidth();
                Bitmap bitmap = Bitmap.createBitmap(
                        image.getWidth() + rowPadding / pixelStride,
                        image.getHeight(),
                        Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(byteBuffer);
                if (bitmap == null) {
                    // This should not happen.
                } else {
                    // Caption
                    String captionedText = imageCaptioner.captionImage(bitmap, rotationDegrees);
                    caption.postValue(captionedText);
                    ttsModule.textToSpeech(captionedText);
                }
                lastAnalysisTime = now;
                image.close();
            }
        });
        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }

    Preview bindPreview(@NonNull ProcessCameraProvider cameraProvider,
                        @NonNull CameraSelector cameraSelector) {
        final PreviewView previewView = findViewById(R.id.previewView);
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(this, cameraSelector, preview);
        return preview;
    }

    void bindText() {
        final TextView textView = findViewById(R.id.captionView);
        caption.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textView.setText(s);
            }
        });
    }
}
