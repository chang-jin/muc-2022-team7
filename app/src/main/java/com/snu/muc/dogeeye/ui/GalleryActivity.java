package com.snu.muc.dogeeye.ui;

import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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

        //
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setDividerPadding(10);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linearLayout);
        photoEntities = getPhotoEntities();

        // Get sorted projects
        List<Project> projectList = pDao.getAllProjectsOrderedByStartTime();

        for (Project project : projectList) {
            List<PhotoEntity> photos = pDao.getPhotoEntities(project.getId());
            if (photos.size() == 0) {
                continue;
            }
            TextView titleTextView = new TextView(this);
            titleTextView.setText(
                    project.getEndTime() + " / " +
                            "Walk " + String.valueOf(project.getId()));
            titleTextView.setTextSize(20);
            linearLayout.addView(titleTextView);
            linearLayout.addView(getTableLayoutWithPhotos(photos));
        }
        setContentView(scrollView);
    }

    TableLayout getTableLayoutWithPhotos(List<PhotoEntity> photos) {
        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setLayoutParams(new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        List<TableRow> tableRows = new ArrayList<>();
        List<ImageView> imageViewList = new ArrayList<>();
        for (int i = 0; i < (photos.size() + NUM_COLUMNS - 1) / NUM_COLUMNS; i++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setWeightSum(1.0f);
            TableLayout.LayoutParams tableRowParams =
                    new TableLayout.LayoutParams();
            tableRowParams.setMargins(30, 30, 30, 30);
            tableRow.setLayoutParams(tableRowParams);
            tableRows.add(tableRow);
            for (int j = 0; j < NUM_COLUMNS; j++) {
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

        for (int i = 0; i < photos.size(); i++) {
            File imageFile = new File(getExternalMediaDirs()[0],
                    photos.get(i).getFileName());
            imageViewList.get(i).setImageURI(
                    Uri.fromFile(imageFile));
        }
        return tableLayout;
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
