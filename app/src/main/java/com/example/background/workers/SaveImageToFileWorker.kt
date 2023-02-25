package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.KEY_IMAGE_URI
import java.text.SimpleDateFormat
import java.util.*

/**
 * Saves the image to a permanent file
 */
private const val TAG = "SaveImageToFileWorker"

class SaveImageToFileWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val title = "Blurred Image"
    private val dateFormatter = SimpleDateFormat(
        "yyyy.MM.dd", Locale.getDefault()
    )

    override fun doWork(): Result {
        // Makes a notification when the work starts and slows down the work so that
        // it's easier to see each WorkRequest start, even on emulated devices
        makeStatusNotification("Saving image", applicationContext)
        sleep()
    TODO("Complete SaveImage doWork")
    }

    private fun saveImage(){
        val resolver = applicationContext.contentResolver
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        val bitmap = BitmapFactory.decodeStream(
            resolver.openInputStream(Uri.parse(resourceUri)))
        val imageUrl = MediaStore.Images.Media.insertImage(
            resolver, bitmap, title, dateFormatter.format(Date()))
    }
}