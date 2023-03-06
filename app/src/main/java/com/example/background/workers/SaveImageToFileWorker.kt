package com.example.background.workers
/*
* https://www.youtube.com/watch?v=nA4XWsG9IPM&ab_channel=Foxandroid
* */
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import java.io.IOException
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

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun doWork(): Result {
        makeStatusNotification("Saving image", applicationContext)
        sleep()
        return saveImage()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveImage(): Result {
        val resolver = applicationContext.contentResolver
        return try {
            val resourceUri = inputData.getString(KEY_IMAGE_URI)
            val bitmap = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri))
            )

            val imageUrl: Uri? = save(UUID.randomUUID().toString(), bitmap)
            Log.d(TAG, "imageUrl: $imageUrl")
            if (imageUrl != null) {
                val output = workDataOf(KEY_IMAGE_URI to imageUrl.toString())
                Log.d(TAG, "saveImage: ${output.getString(KEY_IMAGE_URI)}")
                Result.success(output)
            } else {
                Log.e(TAG, "Writing to MediaStore failed")
                Result.failure()
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            Result.failure()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun save(name: String = "Blurred", bmp: Bitmap): Uri? {
        val resolver = applicationContext.contentResolver
        val imageCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
            else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "${name}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "Image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        val imageUri = resolver.insert(imageCollection, values)
        println(imageUri)
        try {
            if (imageUri != null) {
                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    if (!bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                        throw IOException("Failed to save bitmap.")
                    }
                }
            }
        } catch (e: IOException) {
            if (imageUri != null) resolver.delete(imageUri, null, null)
        }

        return imageUri
    }
}