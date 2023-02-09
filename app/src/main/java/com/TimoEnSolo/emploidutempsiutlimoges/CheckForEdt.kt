package com.TimoEnSolo.emploidutempsiutlimoges

import android.content.Context
import android.util.Log
import androidx.work.*
import java.io.*
import java.net.URL
import java.nio.file.Files
import java.util.*

class CheckForEdt(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        println("CheckForEDT")
        val context = applicationContext
        val yearTarget =
            context.getSharedPreferences("settings", Context.MODE_PRIVATE).getInt("year", 0)
        return try {
            val actualPdfs: List<File> =
                MainActivity.getAllFile(context.filesDir, yearTarget, true)
            val newPdfs = downloadAllPdf(yearTarget, context.cacheDir)
            if (newPdfs.size < actualPdfs.size) {
                return Result.failure()
            }
            val changes = BooleanArray(newPdfs.size)
            checkPdfChanges(actualPdfs, newPdfs, changes)
            for (i in changes.indices) {
                if (!changes[i]) continue
                Notifications.createEdtChangedNotification(context, i + 1)
                Notifications.createEdtChangedSummaryNotification(context)
            }
            if (isThereNewEdt(actualPdfs, newPdfs)) {
                Notifications.createNewEdtNotification(context, newPdfs.size)
                for (i in actualPdfs.size until newPdfs.size) { //add all the new pdfs to changes
                    changes[i] = true
                }
            }
            for (newPdf in newPdfs) {
                newPdf.renameTo(File(context.filesDir, newPdf.name))
            }
            val sharedPreferences =
                applicationContext.getSharedPreferences("EdtChanges", Context.MODE_PRIVATE)
            for (i in changes.indices) {
                if (changes[i] || sharedPreferences.getBoolean(i.toString(), false)) {
                    sharedPreferences.edit().putBoolean(i.toString(), true).apply()
                    changes[i] = true
                }
            }
            MainActivity.instance.get()?.downloadedPdfs(changes)
            Result.success()
        } catch (e: Exception) {
            Log.e("Edt check offline", "Error while checking for edts", e)
            Result.failure()
        }
    }

    private fun isThereNewEdt(actualPdfs: List<File>, newPdfs: List<File>): Boolean {
        return actualPdfs.size < newPdfs.size
    }

    private fun checkPdfChanges(actualPdfs: List<File>, newPdfs: List<File>, changes: BooleanArray) {
        for (i in actualPdfs.indices) {
            try {
                val f1 = Files.readAllBytes(actualPdfs[i].toPath())
                val f2 = Files.readAllBytes(newPdfs[i].toPath())
                changes[i] = !Arrays.equals(f1, f2)
            } catch (e: IOException) {
                e.printStackTrace()
                changes[i] = false
            }
        }
    }

    @Throws(Exception::class)
    fun downloadAllPdf(targetYear: Int, cacheDir: File?): List<File> {
        var name: String
        var i = 1
        name = "A" + (targetYear + 1) + "_S" + i + ".pdf"
        val pdfList: MutableList<File> = ArrayList()
        var actualPdf = File(cacheDir, name)
        while (downloadPdf(
                "http://edt-iut-info.unilim.fr/edt/A" + (targetYear + 1) + "/" + name,
                actualPdf
            )
        ) {
            pdfList.add(actualPdf)
            i++
            name = "A" + (targetYear + 1) + "_S" + i + ".pdf"
            actualPdf = File(cacheDir, name)
        }
        return pdfList
    }

    @Throws(Exception::class)
    fun downloadPdf(url: String?, destination: File): Boolean {
        println("Downloading Pdf...")
        try {
            val u = URL(url)
            val `is` = u.openStream()
            val dis = DataInputStream(`is`)
            val buffer = ByteArray(1024)
            var length: Int
            val fos = FileOutputStream(destination)
            while (dis.read(buffer).also { length = it } > 0) {
                fos.write(buffer, 0, length)
            }
            return true
        } catch (e: FileNotFoundException) {
            Log.w("file not found", destination.name + " not found", e)
        }
        return false
    }
}