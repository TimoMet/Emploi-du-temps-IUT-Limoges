package com.TimoEnSolo.emploidutempsiutlimoges;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConvertPdfsToImages extends Worker {
    private final boolean[] edtsToDownload;
    private final int targetYear;
    private final SharedPreferences sharedPreferences;

    public ConvertPdfsToImages(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        edtsToDownload = workerParams.getInputData().getBooleanArray("edtsToDownload");
        targetYear = workerParams.getInputData().getInt("targetYear", 0);
        sharedPreferences = context.getSharedPreferences("EdtChanges", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public Result doWork() {
        List<File> pdfList = MainActivity.getAllFile(getApplicationContext().getFilesDir(), targetYear, true);
        List<File> pdfListToDownload = new ArrayList<>();

        System.out.println("changes : " + Arrays.toString(edtsToDownload));

        for (int i = 0; i < edtsToDownload.length; i++) {
            if (edtsToDownload[i]) {
                pdfListToDownload.add(pdfList.get(i));
            }
        }

        downloadImages(pdfListToDownload);


        refreshOnMainActivity(true);

        return Result.success();
    }


    private void refreshOnMainActivity(boolean ended) {
        System.out.println("refreshing main activity");
        if (MainActivity.instance == null)
            return;
        MainActivity mainActivity = MainActivity.instance.get();
        mainActivity.runOnUiThread(() -> {
            mainActivity.refreshBitmapList(targetYear);
            if (ended)
                mainActivity.refreshEnded();
        });

    }


    private void downloadImages(List<File> pdfs) {
        System.out.println("Converting to Images... " + pdfs.size());


        for (int i = 0; i < pdfs.size(); i++) {
            File pdf = pdfs.get(i);
            System.out.println("Converting " + pdf.getName() + " ...");

            try {
                PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY));

                PdfRenderer.Page page = renderer.openPage(0);

                double scaleBy = 2048 / (double) page.getHeight();

                int width = (int) (page.getWidth() * scaleBy);
                int height = (int) (page.getHeight() * scaleBy);

                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);


                File outputFile = new File(getApplicationContext().getFilesDir() + "/" + pdf.getName() + ".png");
                FileOutputStream outputStream = new FileOutputStream(outputFile);


                System.out.println("writing bitmap");
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

                outputStream.close();
                page.close();
                renderer.close();

                sharedPreferences.edit().putBoolean(String.valueOf(i), false).apply();


                refreshOnMainActivity(false);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
