package com.unimolix.emploidutempsiutlimoges;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CheckForEdt extends Worker {


    public CheckForEdt(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        System.out.println("CheckForEDT");

        Context context = getApplicationContext();
        int yearTarget = context.getSharedPreferences("settings", Context.MODE_PRIVATE).getInt("year", 0);

        try {
            List<File> actualPdfs = MainActivity.getAllFile(context.getFilesDir(), yearTarget, true);
            List<File> newPdfs = downloadAllPdf(yearTarget, context.getCacheDir());

            if (newPdfs.size() < actualPdfs.size()) {
                return Result.failure();
            }

            boolean[] changes = new boolean[newPdfs.size()];
            checkPdfChanges(actualPdfs, newPdfs, changes);

            for (int i = 0; i < changes.length; i++) {
                if(!changes[i])
                    continue;
                Notifications.createEdtChangedNotification(context, i + 1);
                Notifications.createEdtChangedSummaryNotification(context);
            }

            if (isThereNewEdt(actualPdfs, newPdfs)) {
                Notifications.createNewEdtNotification(context, newPdfs.size());
                for (int i = actualPdfs.size(); i < newPdfs.size(); i++) { //add all the new pdfs to changes
                    changes[i] = true;
                }
            }


            for (File newPdf : newPdfs) {
                newPdf.renameTo(new File(context.getFilesDir(), newPdf.getName()));
            }

            final MainActivity mainActivity = MainActivity.instance.get();

            if (mainActivity != null) {
                mainActivity.downloadedPdfs(changes);
            }


            return Result.success();
        } catch (Exception e) {
            Log.e("Edt check offline", "Error while checking for edts", e);
            return Result.failure();
        }

    }


    public boolean isThereNewEdt(List<File> actualPdfs, List<File> newPdfs) {
        return actualPdfs.size() < newPdfs.size();
    }

    public void checkPdfChanges(List<File> actualPdfs, List<File> newPdfs, boolean[] changes) {
        for (int i = 0; i < actualPdfs.size(); i++) {
            try {
                byte[] f1 = Files.readAllBytes(actualPdfs.get(i).toPath());
                byte[] f2 = Files.readAllBytes(newPdfs.get(i).toPath());
                changes[i] = !Arrays.equals(f1, f2);
            } catch (IOException e) {
                e.printStackTrace();
                changes[i] = false;
            }
        }
    }


    public List<File> downloadAllPdf(int targetYear, File cacheDir) throws Exception {
        String name;
        int i = 1;
        name = "A" + (targetYear + 1) + "_S" + i + ".pdf";

        List<File> pdfList = new ArrayList<>();
        File actualPdf = new File(cacheDir, name);
        while (DownloadPdf("http://edt-iut-info.unilim.fr/edt/A" + (targetYear + 1) + "/" + name, actualPdf)) {
            pdfList.add(actualPdf);

            i++;
            name = "A" + (targetYear + 1) + "_S" + i + ".pdf";
            actualPdf = new File(cacheDir, name);
        }
        return pdfList;
    }


    public boolean DownloadPdf(String url, File destination) throws Exception {
        System.out.println("Downloading Pdf...");
        try {
            URL u = new URL(url);
            InputStream is = u.openStream();

            DataInputStream dis = new DataInputStream(is);

            byte[] buffer = new byte[1024];
            int length;


            FileOutputStream fos = new FileOutputStream(destination);
            while ((length = dis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            return true;

        } catch (FileNotFoundException e) {
            Log.w("file not found", destination.getName() + " not found", e);
        }

        return false;
    }

}