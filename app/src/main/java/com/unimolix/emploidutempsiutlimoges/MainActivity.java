package com.unimolix.emploidutempsiutlimoges;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jsibbold.zoomage.ZoomageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


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

        progressBar = findViewById(R.id.progressBar);
        menuButton = findViewById(R.id.menuButton);
        refreshInMenu = findViewById(R.id.menuRefresh);
        settingInMenu = findViewById(R.id.menuSettings);
        buttonNext = findViewById(R.id.next);
        buttonPrevious = findViewById(R.id.previous);
        zoomImageView = findViewById(R.id.zoomImage);
        /*swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            refresh();
        });
        swipeRefreshLayout.setRefreshing(true);*/

        refreshInMenu.setOnClickListener(v -> {
            closeFABMenu();
            refresh();
        });

        settingInMenu.setOnClickListener(v -> {
            closeFABMenu();
            startActivity(new Intent(this, SettingsActivity.class));
        });


        checkFirstConnection();


        refreshBitmapList();
        refreshImage();
        refresh();
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
            if ("A1".equals(fonts[which])) {
                yearTarget = 0;
            } else if ("A2".equals(fonts[which])) {
                yearTarget = 1;
            } else if ("A3".equals(fonts[which])) {
                yearTarget = 2;
            }
            Toast.makeText(this, "Vous pourrez changer l'année dans les paramètres", Toast.LENGTH_SHORT).show();
        }).setOnCancelListener(dialog -> {
            Toast.makeText(this, "Vous pourrez changer l'année dans les paramètres", Toast.LENGTH_SHORT).show();
        });
        builder.show();
        preferences.edit().putBoolean("hasOpened", true).apply();
    }

    private void checkYearTarget() {
        int year = getSharedPreferences("settings", MODE_PRIVATE).getInt("year", 0);
        if (year != yearTarget) {
            yearTarget = year;
            Toast.makeText(this, "Changement d'année : A" + (year + 1), Toast.LENGTH_SHORT).show();
            refreshBitmapList();
            refreshImage();
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
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        new RefreshPdfList(yearTarget, getFilesDir(), getCacheDir(), height, width, this::onRefresh).execute(this);

    }

    public void onRefresh() {
        refreshBitmapList();
        refreshImage();
        progressBar.setVisibility(View.GONE);
        //swipeRefreshLayout.setRefreshing(false);
    }

    private void refreshImage() {
        System.out.println("refreshImages : " + images.size() + " images; containing null : " + images.contains(null));

        if (images.size() == 0) {
            return;
        }
        zoomImageView.setImageBitmap(images.get(currentImage));
        updateButtons();
    }

    public void refreshBitmapList() {
        System.out.println("refreshBitmapList");

        images.clear();

        String name;
        int i = 0;
        i++;
        name = "A" + (yearTarget + 1) + "_S" + i + ".pdf.png";
        while (new File(getFilesDir() + "/" + name).exists()) {
            try {
                System.out.println("loading " + getFilesDir() + "/" + name);
                File file = new File(getFilesDir() + "/" + name);
                InputStream inputStream = new FileInputStream(file);

                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(inputStream, false);
                Bitmap bitmap = decoder.decodeRegion(new Rect(0, 0, decoder.getWidth(), decoder.getHeight()), new BitmapFactory.Options());
                Matrix matrix = new Matrix();

                matrix.postRotate(90);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                images.add(rotatedBitmap);


            } catch (IOException e) {
                e.printStackTrace();
            }
            i++;
            name = "A" + (yearTarget + 1) + "_S" + i + ".pdf.png";
        }
        if (currentImage == -1) {
            currentImage = images.size() - 1;
        }
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

}