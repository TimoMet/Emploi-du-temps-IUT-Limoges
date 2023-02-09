package com.TimoEnSolo.emploidutempsiutlimoges

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jsibbold.zoomage.ZoomageView
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    //SwipeRefreshLayout swipeRefreshLayout;
    private var zoomImageView: ZoomageView? = null
    private var buttonPrevious: FloatingActionButton? = null
    private var buttonNext: FloatingActionButton? = null
    private var menuButton: FloatingActionButton? = null
    private var refreshInMenu: FloatingActionButton? = null
    private var settingInMenu: FloatingActionButton? = null
    private var progressBar: ProgressBar? = null
    private val images: MutableList<Bitmap> = ArrayList()
    private var currentImage = -1
    private var isFABOpen = false
    private var yearTarget = 0
    private var isStarting = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        instance = WeakReference(this)
        progressBar = findViewById(R.id.progressBar)
        menuButton = findViewById(R.id.menuButton)
        refreshInMenu = findViewById(R.id.menuRefresh)
        settingInMenu = findViewById(R.id.menuSettings)
        buttonNext = findViewById(R.id.next)
        buttonPrevious = findViewById(R.id.previous)
        zoomImageView = findViewById(R.id.zoomImage)
        refreshInMenu!!.setOnClickListener {
            closeFABMenu()
            refresh()
        }
        settingInMenu!!.setOnClickListener {
            closeFABMenu()
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        checkFirstConnection()
        yearTarget = getSharedPreferences("settings", MODE_PRIVATE).getInt("year", 0)
        Notifications.createNotificationChannels(this)
        checkInBackground()
        refreshBitmapList(yearTarget)
        refresh()
    }

    private fun checkInBackground() {
        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(CheckForEdt::class.java, 15, TimeUnit.MINUTES)
                .build()

        println("Enqueue Check Edt")


        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "Check Edt",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest
        )
    }

    fun downloadedPdfs(changes: BooleanArray?) {

        println("Enqueue Convert Pdfs To Images")

        val workRequest: WorkRequest = OneTimeWorkRequest.Builder(ConvertPdfsToImages::class.java)
            .setInputData(
                Data.Builder()
                    .putBooleanArray("edtsToDownload", changes!!)
                    .putInt("targetYear", yearTarget)
                    .build()
            )
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun checkFirstConnection() {
        val preferences = getSharedPreferences("infos", MODE_PRIVATE)
        if (preferences.getBoolean("hasOpened", false)) {
            return
        }
        val fonts = arrayOf(
            "A1", "A2", "A3"
        )
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Quelle est ton année ?")
        builder.setItems(fonts) { _: DialogInterface?, which: Int ->
            yearTarget = which
            getSharedPreferences("settings", MODE_PRIVATE).edit().putInt("year", which).apply()
            Toast.makeText(
                this,
                "Vous pourrez changer l'année dans les paramètres",
                Toast.LENGTH_SHORT
            ).show()
            refreshBitmapList(yearTarget)
            refresh()
        }.setOnCancelListener {
            Toast.makeText(
                this,
                "Vous pourrez changer l'année dans les paramètres",
                Toast.LENGTH_SHORT
            ).show()
        }
        builder.show()
        preferences.edit().putBoolean("hasOpened", true).apply()
    }

    private fun checkYearTarget() {
        val year = getSharedPreferences("settings", MODE_PRIVATE).getInt("year", 0)
        if (year != yearTarget) {
            yearTarget = year
            currentImage = 0
            Toast.makeText(this, "Changement d'année : A" + (year + 1), Toast.LENGTH_SHORT).show()
            refreshBitmapList(yearTarget)
            refresh()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isStarting) {
            isStarting = false
            return
        }
        checkYearTarget()
    }

    fun updateMenu(@Suppress("UNUSED_PARAMETER") view: View?) {
        if (!isFABOpen) {
            showFABMenu()
        } else {
            closeFABMenu()
        }
    }

    private fun showFABMenu() {
        isFABOpen = true
        refreshInMenu!!.animate().translationX(-resources.getDimension(R.dimen.standard_50))
        settingInMenu!!.animate().translationX(-resources.getDimension(R.dimen.standard_100))
    }

    private fun closeFABMenu() {
        isFABOpen = false
        refreshInMenu!!.animate().translationX(0f)
        settingInMenu!!.animate().translationX(0f)
        menuButton!!.translationZ = 1f
    }

    private fun refresh() {
        progressBar!!.visibility = View.VISIBLE
        checkInBackground()
    }

    private fun refreshImage() {
        if (images.size == 0) {
            return
        }
        if (currentImage >= images.size) {
            currentImage = 0
        }
        zoomImageView!!.setImageBitmap(images[currentImage])
        getSharedPreferences("infos", MODE_PRIVATE).edit().putInt("lastPosition", currentImage)
            .apply()
        updateButtons()
    }

    fun refreshBitmapList(year: Int) {
        println("refreshBitmapList")
        if (year != yearTarget) {
            return
        }
        images.clear()
        for (file in getAllFile(filesDir, yearTarget, false)) {
            try {
                println("loading " + file.absolutePath)
                val inputStream: InputStream = FileInputStream(file)

                //deprecated but has to be used for under API 28
                @Suppress("DEPRECATION")
                val decoder = BitmapRegionDecoder.newInstance(inputStream, false)
                val bitmap = decoder!!.decodeRegion(
                    Rect(0, 0, decoder.width, decoder.height),
                    BitmapFactory.Options()
                )
                val matrix = Matrix()
                matrix.postRotate(90f)
                images.add(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (currentImage == -1) {
            val backToLastPosition = getSharedPreferences("settings", MODE_PRIVATE).getBoolean(
                "backToLastPosition",
                true
            )
            if (backToLastPosition) {
                currentImage = getSharedPreferences("infos", MODE_PRIVATE).getInt(
                    "lastPosition",
                    images.size - 1
                )
                if (currentImage >= images.size) {
                    currentImage = images.size - 1
                }
            } else {
                currentImage = images.size - 1
            }
        }
        refreshImage()
    }

    fun previousImage(@Suppress("UNUSED_PARAMETER") view: View?) {
        if (currentImage > 0) {
            currentImage--
            refreshImage()
        }
    }

    fun nextImage(@Suppress("UNUSED_PARAMETER") view: View?) {
        if (currentImage < images.size - 1) {
            currentImage++
            refreshImage()
        }
    }

    private fun updateButtons() {
        buttonPrevious!!.visibility =
            if (currentImage <= 0) View.INVISIBLE else View.VISIBLE
        buttonNext!!.visibility =
            if (currentImage >= images.size - 1) View.INVISIBLE else View.VISIBLE
    }

    fun refreshEnded() {
        progressBar!!.visibility = View.GONE
    }

    companion object {
        var instance: WeakReference<MainActivity> = WeakReference(null)
        fun getAllFile(fileDir: File, yearTarget: Int, justPdf: Boolean): List<File> {
            val files: MutableList<File> = ArrayList()
            var i = 1
            var actualFile =
                File(fileDir.toString() + "/" + "A" + (yearTarget + 1) + "_S" + i + ".pdf" + if (justPdf) "" else ".png")
            while (actualFile.exists()) {
                files.add(actualFile)
                i++
                actualFile =
                    File(fileDir.toString() + "/" + "A" + (yearTarget + 1) + "_S" + i + ".pdf" + if (justPdf) "" else ".png")
            }
            return files
        }
    }

}