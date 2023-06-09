package com.huji.ARfurnitureplanner.utils


import android.app.Activity
import android.content.ContentValues
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.view.PixelCopy
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.ar.sceneform.ArSceneView
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/***
 * Takes picture with  ar module
 */
class PhotoSaver(
    private val activity: Activity
) {
    private var title: String = ""
    private var description = "taken with CouchMirage"

    private fun generateFilename(): String? {
        val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        title = "/couch_mirage_screenshot_${date}.jpg"

        return activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath + title
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveBitmapToGallery(bmp: Bitmap) {
        val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${date}_screenshot.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/CouchMirage")
        }

        val uri = activity.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        activity.contentResolver.openOutputStream(uri ?: return).use { outputStream ->
            outputStream?.let {
                try {
                    saveDataToGallery(bmp, outputStream)
                } catch (e: IOException) {
                    Toast.makeText(activity, "Failed to save bitmap to gallery.", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

        generateFilename()

    }

    private fun saveBitmapToGallery(bmp: Bitmap, filename: String) {
        val out = File(filename)
        if (!out.parentFile.exists()) {
            out.parentFile.mkdirs()
            Log.d("PhotoSaver", "creating folder:${out.absolutePath}")
        }
        try {
            Log.d("PhotoSaver", "Saving to  folder:${out.absolutePath}")

            val outputStream = FileOutputStream(filename)
            saveDataToGallery(bmp, outputStream)
            MediaScannerConnection.scanFile(activity, arrayOf(filename), null, null)


        } catch (e: IOException) {
            Toast.makeText(activity, "Failed to save bitmap to gallery.", Toast.LENGTH_LONG).show()
            Log.d("PhotoSaver", "Failed to save bitmap to gallery. ${e.message}")

        }
    }

    private fun saveDataToGallery(bmp: Bitmap, outputStream: OutputStream) {
        val outputData = ByteArrayOutputStream()

        MediaStore.Images.Media.insertImage(
            activity.contentResolver, bmp,
            title,
            description
        );


        bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputData)
        outputData.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
    }

    fun takePhoto(arSceneView: ArSceneView) {
        val bmp =
            Bitmap.createBitmap(arSceneView.width, arSceneView.height, Bitmap.Config.ARGB_8888)
        val handlerThread = HandlerThread("PixelCopyThread")
        handlerThread.start()

        PixelCopy.request(arSceneView, bmp, { result ->
            if (result == PixelCopy.SUCCESS) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    Log.d("PhotoSaver", "Build.VERSION.SDK_INT <= Build.VERSION_CODES.P")
                    val filename = generateFilename()
                    saveBitmapToGallery(bmp, filename ?: return@request)
                } else {
                    saveBitmapToGallery(bmp)
                }
                activity.runOnUiThread {

                    //


                    Toast.makeText(activity, "Successfully took photo!", Toast.LENGTH_LONG).show()


                }
            } else {
                activity.runOnUiThread {
                    Toast.makeText(activity, "Failed to take photo.", Toast.LENGTH_LONG).show()
                }
            }
            handlerThread.quitSafely()
        }, Handler(handlerThread.looper))
    }

}