package com.snu.muc.dogeeye.ui;

import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.snu.muc.dogeeye.R;
import com.snu.muc.dogeeye.common.TextSpeechModule;
import com.snu.muc.dogeeye.model.PhotoEntity;
import com.snu.muc.dogeeye.model.Project;
import com.snu.muc.dogeeye.model.ProjectDB;
import com.snu.muc.dogeeye.model.ProjectDao;
import com.snu.muc.dogeeye.ui.photo.ImageCaptioner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GalleryActivity extends AppCompatActivity {
    private final String TAG = "GalleryActivity";
    private final int NUM_COLUMNS = 2;
    private ProjectDB pdb;
    private ProjectDao pDao;
    private TableLayout tableLayout;
    private List<TableRow> tableRows = new ArrayList<>();
    private List<ImageView> imageViewList = new ArrayList<>();
    private Map<Integer, List<PhotoEntity>> photoEntities;
    private ImageCaptioner imageCaptioner;
    private TextSpeechModule ttsModule;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Database initialization
        pdb = ProjectDB.getProjectDB(this);
        pDao = pdb.projectDao();

        // Get imageCaptioner
        try {
            imageCaptioner = ImageCaptioner.getInstance(
                    this,
                    ImageCaptioner.Model.MOBILENET_GRU,
                    ImageCaptioner.Device.NNAPI,
                    4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get TTS module
        ttsModule = TextSpeechModule.getInstance();

        tableLayout = new TableLayout(this);
        tableLayout.setLayoutParams(new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        photoEntities = getPhotoEntities();
        // Calculate how many table rows are needed.
        // Assume each row has two columns.
        int totalNumRows = 0;
        List<PhotoEntity> flattenedPhotoEntities = new ArrayList<>();
        for (Project project : pDao.getAllProjects()) {
            totalNumRows += (photoEntities.get(project.getId()).size() + NUM_COLUMNS - 1) / NUM_COLUMNS;
            flattenedPhotoEntities.addAll(photoEntities.get(project.getId()));
        }
        // Add table rows.
        for (int row = 0; row < totalNumRows; row++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setWeightSum(1.0f);
            TableLayout.LayoutParams tableRowParams =
                    new TableLayout.LayoutParams();
            tableRowParams.setMargins(30, 30, 30, 30);
            tableRow.setLayoutParams(tableRowParams);
            tableRows.add(tableRow);
            for (int i = 0; i < NUM_COLUMNS; i++) {
                ImageView imageView = new ImageView(this);
                TableRow.LayoutParams imageViewParams =
                        new TableRow.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                0.5f);
                imageViewParams.setMargins(30, 0, 30, 0);
                imageView.setLayoutParams(imageViewParams);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setOnClickListener(getImageViewOnClickListener());
                imageViewList.add(imageView);
                tableRow.addView(imageView);
            }
            tableLayout.addView(tableRow);
        }
        // Set images
        for (int i = 0; i < flattenedPhotoEntities.size(); i++) {
            File imageFile = new File(getExternalMediaDirs()[0],
                    flattenedPhotoEntities.get(i).getFileName());
            Log.d(TAG, imageFile.getAbsolutePath());
            imageViewList.get(i).setImageURI(
                    Uri.fromFile(imageFile));

        }
        setContentView(tableLayout);
    }

    View.OnClickListener getImageViewOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView imageView = (ImageView) view;
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                ttsModule.textToSpeech(imageCaptioner.captionImage(bitmapDrawable.getBitmap(), 0));
            }
        };
    }

    Map<Integer, List<PhotoEntity>> getPhotoEntities() {
        List<Project> projects = pDao.getAllProjects();
        Map<Integer, List<PhotoEntity>> resultEntities = new HashMap<>();
        for (Project project : projects) {
            int projectId = project.getId();
            pDao.getPhotoEntities(projectId);
            resultEntities.put(projectId, pDao.getPhotoEntities(projectId));
        }
        return resultEntities;
    }
}
