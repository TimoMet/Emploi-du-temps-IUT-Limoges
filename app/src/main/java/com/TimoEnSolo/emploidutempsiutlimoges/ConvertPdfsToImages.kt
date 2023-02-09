package com.TimoEnSolo.emploidutempsiutlimoges

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class ConvertPdfsToImages(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    private val edtsToDownload: BooleanArray?
    private val targetYear: Int
    private val sharedPreferences: SharedPreferences

    init {
        edtsToDownload = workerParams.inputData.getBooleanArray("edtsToDownload")
        targetYear = workerParams.inputData.getInt("targetYear", 0)
        sharedPreferences = context.getSharedPreferences("EdtChanges", Context.MODE_PRIVATE)
    }

    override fun doWork(): Result {
        val pdfList: List<File> = MainActivity.getAllFile(
            applicationContext.filesDir, targetYear, true
        )
        val pdfListToDownload: MutableList<File> = ArrayList()
        println("changes : " + Arrays.toString(edtsToDownload))
        for (i in edtsToDownload!!.indices) {
            if (edtsToDownload[i]) {
                pdfListToDownload.add(pdfList[i])
            }
        }
        downloadImages(pdfListToDownload)
        refreshOnMainActivity(true)
        return Result.success()
    }

    private fun refreshOnMainActivity(ended: Boolean, indexEdt: Int = -1 ) {
        println("refreshing main activity")
        val mainActivity: MainActivity = MainActivity.instance.get() ?: return

        mainActivity.runOnUiThread {
            if (indexEdt != -1)
                mainActivity.refreshBitmapByIndex(targetYear, indexEdt)
            else
                mainActivity.refreshBitmapList(targetYear)
            if (ended) mainActivity.refreshEnded()
        }
    }

    private fun downloadImages(pdfs: List<File>) {
        println("Converting to Images... " + pdfs.size)
        for (i in pdfs.indices) {
            val pdf = pdfs[i]
            println("Converting " + pdf.name + " ...")
            try {
                val renderer =
                    PdfRenderer(ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY))
                val page = renderer.openPage(0)
                val scaleBy = 2048 / page.height.toDouble()
                val width = (page.width * scaleBy).toInt()
                val height = (page.height * scaleBy).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                val outputFile =
                    File(applicationContext.filesDir.toString() + "/" + pdf.name + ".png")
                val outputStream = FileOutputStream(outputFile)
                println("writing bitmap")
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
                page.close()
                renderer.close()
                sharedPreferences.edit().putBoolean(i.toString(), false).apply()
                refreshOnMainActivity(false, i)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}