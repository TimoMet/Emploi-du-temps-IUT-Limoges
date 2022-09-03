package com.unimolix.emploidutempsiutlimoges;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Xml;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.otaliastudios.zoom.ZoomImageView;
import com.otaliastudios.zoom.ZoomLayout;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Bitmap> images = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;
    ImageView imageView;
    ZoomImageView zoomImageView;
    int currentImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        zoomImageView = findViewById(R.id.zoomImage);
        imageView = findViewById(R.id.edtImage);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            refresh();
        });

        refreshBitmapList();
        currentImage = images.size() - 1;
        refreshImage();
    }

    public void refresh() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        new RefreshPdfList(getFilesDir(), getCacheDir(), height, width, this::onRefresh).execute(getContentResolver());

    }

    public void onRefresh() {
        refreshBitmapList();
        refreshImage();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void refreshImage() {
        System.out.println("refreshImages : " + images.size() + " images; containing null : " + images.contains(null));

        imageView.setImageBitmap(images.get(currentImage));
        zoomImageView.setImageBitmap(images.get(currentImage));
    }

    public void refreshBitmapList() {
        System.out.println("refreshBitmapList");

        images.clear();

        String name;
        int i = 0;
        i++;
        name = "A2_S" + i + ".pdf.png";
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
            name = "A2_S" + i + ".pdf.png";
        }
    }
}