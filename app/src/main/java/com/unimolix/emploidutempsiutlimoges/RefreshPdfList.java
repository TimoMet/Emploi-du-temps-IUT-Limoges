package com.unimolix.emploidutempsiutlimoges;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.FileUtils;
import android.util.Log;
import android.widget.Toast;

import org.vudroid.core.DecodeServiceBase;
import org.vudroid.pdfdroid.codec.PdfContext;
import org.vudroid.pdfdroid.codec.PdfPage;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RefreshPdfList extends AsyncTask<Activity, Integer, Integer> {

    private final File filesDir;
    private final File cacheDir;
    private final Runnable onEnd;
    private final int pixelHeight;
    private final int pixelWidth;
    private final int targetYear;


    public RefreshPdfList(int targetYear, File filesDir, File cacheDir, int pixelHeight, int pixelWidth, Runnable onEnd) {
        this.targetYear = targetYear;
        this.filesDir = filesDir;
        this.cacheDir = cacheDir;
        this.pixelHeight = pixelHeight;
        this.pixelWidth = pixelWidth;
        this.onEnd = onEnd;
    }

    @Override
    protected Integer doInBackground(Activity... contexts) {
        try {
            List<File> pdfList = new ArrayList<>();


            String name;
            int i = 1;
            name = "A" + (targetYear + 1) + "_S" + i + ".pdf";


            while (DownloadPdf("http://edt-iut-info.unilim.fr/edt/A" + (targetYear + 1) + "/" + name, name)) {
                pdfList.add(new File(filesDir + "/" + name));
                i++;
                name = "A" + (targetYear + 1) + "_S" + i + ".pdf";
            }

            downloadImages(pdfList, contexts[0].getContentResolver());

            if (i == 1) {
                contexts[0].runOnUiThread(() -> Toast.makeText(contexts[0], "No edt found. Check connection", Toast.LENGTH_SHORT).show());
            }
            return i - 1;
        } catch (Exception e) {
            Log.e("RefreshPdfList", e.getMessage());
            return 0;
        }
    }


    @Override
    protected void onPostExecute(Integer s) {
        super.onPostExecute(s);
        onEnd.run();
    }

    public boolean DownloadPdf(String url, String fileName) {
        System.out.println("Downloading Pdf...");
        try {
            URL u = new URL(url);
            InputStream is = u.openStream();

            DataInputStream dis = new DataInputStream(is);

            byte[] buffer = new byte[1024];
            int length;

            File file = new File(filesDir + "/" + fileName);
            File cacheFile = new File(cacheDir + "/" + fileName);
            FileOutputStream fos = new FileOutputStream(cacheFile);
            while ((length = dis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            cacheFile.renameTo(file);
            System.out.println("PDF " + file.getAbsolutePath() + " téléchargé");
            return true;

        } catch (FileNotFoundException e) {
            Log.w("file not found", fileName + " not found", e);
        } catch (MalformedURLException mue) {
            Log.e("SYNC getUpdate", "malformed url error", mue);
        } catch (IOException ioe) {
            Log.e("SYNC getUpdate", "io error", ioe);
        } catch (SecurityException se) {
            Log.e("SYNC getUpdate", "security error", se);
        }
        return false;
    }

    private void downloadImages(List<File> pdfs, ContentResolver content) {
        System.out.println("Converting to Images...");


        for (File pdf : pdfs) {
            DecodeServiceBase decodeService = new DecodeServiceBase(new PdfContext());
            decodeService.setContentResolver(content);
            decodeService.open(Uri.fromFile(pdf));

            int pageCount = decodeService.getPageCount();
            for (int i = 0; i < pageCount; i++) {
                PdfPage page = (PdfPage) decodeService.getPage(i);
                RectF rectF = new RectF(0, 0, 1, 1);


                double scaleBy = Math.min(pixelWidth * 2 / (double) page.getWidth(), //
                        pixelHeight * 2 / (double) page.getHeight());
                int with = (int) (page.getWidth() * scaleBy);
                int height = (int) (page.getHeight() * scaleBy);

                Bitmap bitmap = page.renderBitmap(with, height, rectF);

                try {
                    File outputFile = new File(filesDir + "/" + pdf.getName() + ".png");
                    FileOutputStream outputStream = new FileOutputStream(outputFile);


                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
