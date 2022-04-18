package com.snu.muc.dogeeye.ui.photo;

import static java.lang.Thread.sleep;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.common.util.concurrent.ListenableFuture;
import com.snu.muc.dogeeye.databinding.FragmentPhotoBinding;

import com.snu.muc.dogeeye.ui.photo.ImageCaptioner.Device;
import com.snu.muc.dogeeye.ui.photo.ImageCaptioner.Model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class PhotoFragment extends Fragment {
    private final String TAG = "PhotoFragment";

    private ImageCaptioner imageCaptioner;
    private PhotoViewModel photoViewModel;
    private FragmentPhotoBinding binding;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private int imageSizeX;
    private int imageSizeY;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // create tflite image captioner
        createImageCaptioner(Model.MOBILENET_GRU, Device.CPU, 1);
        if (imageCaptioner == null) {
            Log.d(TAG, "Failed to load image captioner.");
        } else {
            Log.d(TAG, "Success to load image captioner.");
        }

        // create view model for photo view
        photoViewModel =
                new ViewModelProvider(this).get(PhotoViewModel.class);
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());

        binding = FragmentPhotoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        // Bind camera preview
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider, cameraSelector);
            } catch (ExecutionException | InterruptedException e) {
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(getContext()));

        // Bind text view
        bindText();
        return root;
    }

    private void createImageCaptioner(Model model, Device device, int numThreads) {
        if (imageCaptioner != null) {
            return;
        }
        try {
            imageCaptioner = ImageCaptioner.create(getActivity(), model, device, numThreads);
        } catch (IOException | IllegalArgumentException e) {
            Log.e(TAG, e.toString());
        }

        imageSizeX = imageCaptioner.getImageSizeX();
        imageSizeY = imageCaptioner.getImageSizeY();
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider,
                     @NonNull CameraSelector cameraSelector) {
        final PreviewView previewView = binding.previewView;
        final ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .setTargetResolution(new Size(224, 224))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                // image is JPEG, so need to decode to obtain bitmap
                ImageProxy.PlaneProxy plane = imageProxy.getPlanes()[0];
                ByteBuffer byteBuffer = imageProxy.getPlanes()[0].getBuffer();
                int pixelStride = plane.getPixelStride();
                int rowStride = plane.getRowStride();
                int rowPadding = rowStride - pixelStride * imageProxy.getWidth();
                Bitmap bitmap = Bitmap.createBitmap(
                        imageProxy.getWidth() + rowPadding / pixelStride,
                        imageProxy.getHeight(),
                        Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(byteBuffer);
                if (bitmap == null) {
                    Log.d(TAG, "bitmap is null!");
                } else {
                    imageCaptioner.captionImage(bitmap, rotationDegrees);
                    photoViewModel.postText(String.valueOf(rotationDegrees));
                }
                imageProxy.close();
            }
        });
        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview);
        cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, imageAnalysis, preview);
    }

    void bindText() {
        final TextView textView = binding.textView;
        photoViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textView.setText(s);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}