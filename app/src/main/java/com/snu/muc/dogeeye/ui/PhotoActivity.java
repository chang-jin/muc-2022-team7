package com.snu.muc.dogeeye.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.common.util.concurrent.ListenableFuture;
import com.snu.muc.dogeeye.R;
import com.snu.muc.dogeeye.common.TextSpeechModule;
import com.snu.muc.dogeeye.model.PhotoEntity;
import com.snu.muc.dogeeye.model.Project;
import com.snu.muc.dogeeye.model.ProjectDB;
import com.snu.muc.dogeeye.model.ProjectDao;
import com.snu.muc.dogeeye.ui.photo.ImageCaptioner;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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

    private ProjectDB pdb;
    private ProjectDao pDao;
    private int currentProjectId;

    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private ImageAnalysis imageAnalysis;
    private ImageCapture imageCapture;
    private Preview preview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        // Database initialization
        pdb = ProjectDB.getProjectDB(this);
        pDao = pdb.projectDao();
        currentProjectId = getIntent().getIntExtra("currentProjectId", -1);

        // Bind caption text view
        bindText();

        // Prepare camera
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(getIntent().getIntExtra("facing", 1))
                .build();

        // Bind preview
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider, cameraSelector);
                if (getIntent().getIntExtra("facing", 1) == 1) {
                    bindCaptioning(cameraProvider, cameraSelector);
                }
                bindCapture(cameraProvider, cameraSelector);
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
                        @NonNull CameraSelector cameraSelector) {
        imageAnalysis =
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
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider,
                     @NonNull CameraSelector cameraSelector) {
        final PreviewView previewView = findViewById(R.id.previewView);
        preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
    }

    void bindCapture(@NonNull ProcessCameraProvider cameraProvider,
                     @NonNull CameraSelector cameraSelector) {
        final Button captureButton = findViewById(R.id.captureButton);
        imageCapture =
                new ImageCapture.Builder().build();
        if (getIntent().getIntExtra("facing", 1) == 1) {
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
        } else {
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        }

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, String.valueOf(getExternalMediaDirs()[0]));
                ImageCapture.OutputFileOptions outputFileOptions =
                        new ImageCapture.OutputFileOptions.Builder(
                                new File(
                                        getExternalMediaDirs()[0],
                                        String.valueOf(System.currentTimeMillis()) + ".jpg")).build();
                imageCapture.takePicture(outputFileOptions, getMainExecutor(),
                        new ImageCapture.OnImageSavedCallback() {
                            @Override
                            public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                                Uri savedUri = outputFileResults.getSavedUri();
                                List<String> pathSegments = savedUri.getPathSegments();
                                // DB save
                                addNewPhoto(pathSegments.get(pathSegments.size() - 1));
                            }

                            @Override
                            public void onError(@NonNull ImageCaptureException exception) {
                                Log.e(TAG, "FILE NOT SAVED!!!!");
                            }
                        }
                );
            }
        });
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

    void addNewPhoto(String fileName) {
        PhotoEntity photoEntity = new PhotoEntity();
        long mNow = System.currentTimeMillis();
        Date mDate = new Date(mNow);
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        photoEntity.setPID(currentProjectId);
        photoEntity.setFileName(fileName);
        photoEntity.setFilePath((getExternalMediaDirs()[0].toString()));
        photoEntity.setCreatedAt(mFormat.format(mDate));
        pDao.addPhoto(photoEntity);
    }
}
