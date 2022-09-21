package com.unimolix.emploidutempsiutlimoges;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jsibbold.zoomage.ZoomageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static WeakReference<MainActivity> instance;

    //SwipeRefreshLayout swipeRefreshLayout;
    private ZoomageView zoomImageView;
    private FloatingActionButton buttonPrevious;
    private FloatingActionButton buttonNext;

    private FloatingActionButton menuButton;
    private FloatingActionButton refreshInMenu;
    private FloatingActionButton settingInMenu;

    private ProgressBar progressBar;

    private final List<Bitmap> images = new ArrayList<>();
    private int currentImage = -1;
    private boolean isFABOpen;

    private int yearTarget = 0;

    private boolean isStarting = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = new WeakReference<>(this);

        progressBar = findViewById(R.id.progressBar);
        menuButton = findViewById(R.id.menuButton);
        refreshInMenu = findViewById(R.id.menuRefresh);
        settingInMenu = findViewById(R.id.menuSettings);
        buttonNext = findViewById(R.id.next);
        buttonPrevious = findViewById(R.id.previous);
        zoomImageView = findViewById(R.id.zoomImage);

        refreshInMenu.setOnClickListener(v -> {
            closeFABMenu();
            refresh();
        });

        settingInMenu.setOnClickListener(v -> {
            closeFABMenu();
            startActivity(new Intent(this, SettingsActivity.class));
        });


        checkFirstConnection();

        yearTarget = getSharedPreferences("settings", MODE_PRIVATE).getInt("year", 0);

        Notifications.createNotificationChannels(this);

        checkInBackground();

        refreshBitmapList(yearTarget);

        refresh();
    }


    private void checkInBackground() {
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(CheckForEdt.class, 15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork("Check Edt", ExistingPeriodicWorkPolicy.REPLACE ,periodicWorkRequest);
    }

    public void downloadedPdfs(boolean[] changes) {

        SharedPreferences sharedPreferences = getSharedPreferences("EdtChanges", MODE_PRIVATE);

        for (int i = 0; i < changes.length; i++) {
            if (changes[i] || sharedPreferences.getBoolean(String.valueOf(i), false)) {
                sharedPreferences.edit().putBoolean(String.valueOf(i), true).apply();
                changes[i] = true;
            }
        }

        WorkRequest workRequest = new OneTimeWorkRequest.Builder(ConvertPdfsToImages.class)
                .setInputData(new Data.Builder()
                        .putBooleanArray("edtsToDownload", changes)
                        .putInt("targetYear", yearTarget)
                        .build())
                .build();

        WorkManager.getInstance(this).enqueue(workRequest);

    }

    private void checkFirstConnection() {
        SharedPreferences preferences = getSharedPreferences("infos", MODE_PRIVATE);
        if (preferences.getBoolean("hasOpened", false)) {
            return;
        }
        final String[] fonts = {
                "A1", "A2", "A3"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quelle est ton année ?");
        builder.setItems(fonts, (dialog, which) -> {
            yearTarget = which;
            getSharedPreferences("settings", MODE_PRIVATE).edit().putInt("year", which).apply();
            Toast.makeText(this, "Vous pourrez changer l'année dans les paramètres", Toast.LENGTH_SHORT).show();
            refreshBitmapList(yearTarget);
            refresh();
        }).setOnCancelListener(dialog -> Toast.makeText(this, "Vous pourrez changer l'année dans les paramètres", Toast.LENGTH_SHORT).show());
        builder.show();
        preferences.edit().putBoolean("hasOpened", true).apply();
    }

    private void checkYearTarget() {
        int year = getSharedPreferences("settings", MODE_PRIVATE).getInt("year", 0);
        if (year != yearTarget) {
            yearTarget = year;
            Toast.makeText(this, "Changement d'année : A" + (year + 1), Toast.LENGTH_SHORT).show();
            refreshBitmapList(yearTarget);
            refresh();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isStarting) {
            isStarting = false;
            return;
        }
        checkYearTarget();
    }

    public void updateMenu(View view) {
        if (!isFABOpen) {
            showFABMenu();
        } else {
            closeFABMenu();
        }
    }

    private void showFABMenu() {
        isFABOpen = true;
        refreshInMenu.animate().translationX(-getResources().getDimension(R.dimen.standard_50));
        settingInMenu.animate().translationX(-getResources().getDimension(R.dimen.standard_100));
    }

    private void closeFABMenu() {
        isFABOpen = false;
        refreshInMenu.animate().translationX(0);
        settingInMenu.animate().translationX(0);
        menuButton.setTranslationZ(1);
    }

    public void refresh() {
        progressBar.setVisibility(View.VISIBLE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        checkInBackground();
    }


    public void refreshImage() {
        if (images.size() == 0) {
            return;
        }
        zoomImageView.setImageBitmap(images.get(currentImage));
        updateButtons();
    }

    public void refreshBitmapList(int year) {
        System.out.println("refreshBitmapList");
        if (year != yearTarget) {
            return;
        }

        images.clear();


        for (File file : getAllFile(getFilesDir(), yearTarget, false)) {
            try {
                System.out.println("loading " + file.getAbsolutePath());
                InputStream inputStream = new FileInputStream(file);

                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(inputStream, false);
                Bitmap bitmap = decoder.decodeRegion(new Rect(0, 0, decoder.getWidth(), decoder.getHeight()), new BitmapFactory.Options());
                Matrix matrix = new Matrix();

                matrix.postRotate(90);

                images.add(bitmap);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (currentImage == -1) {
            currentImage = images.size() - 1;
        }

        refreshImage();
    }

    public static List<File> getAllFile(File fileDir, int yearTarget, boolean justPdf) {
        List<File> files = new ArrayList<>();
        int i = 1;
        File actualFile = new File(fileDir + "/" + "A" + (yearTarget + 1) + "_S" + i + ".pdf" + (justPdf ? "" : ".png"));
        while (actualFile.exists()) {
            files.add(actualFile);
            i++;
            actualFile = new File(fileDir + "/" + "A" + (yearTarget + 1) + "_S" + i + ".pdf" + (justPdf ? "" : ".png"));
        }
        return files;
    }

    public void previousImage(View view) {
        if (currentImage > 0) {
            currentImage--;
            refreshImage();
        }
    }

    public void nextImage(View view) {
        if (currentImage < images.size() - 1) {
            currentImage++;
            refreshImage();
        }
    }

    private void updateButtons() {
        buttonPrevious.setVisibility(currentImage <= 0 ? View.INVISIBLE : View.VISIBLE);
        buttonNext.setVisibility(currentImage >= images.size() - 1 ? View.INVISIBLE : View.VISIBLE);
    }

    public void refreshEnded() {
        progressBar.setVisibility(View.GONE);
    }
}