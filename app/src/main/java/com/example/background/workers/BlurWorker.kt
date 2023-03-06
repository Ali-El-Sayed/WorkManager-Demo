package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import com.example.background.R

private const val TAG: String = "BlurWorker"

class BlurWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    private fun blur(): Data {
        val appContext = applicationContext
        val resourceUri = inputData.getString(KEY_IMAGE_URI)

        if (TextUtils.isEmpty(resourceUri)) {
            Log.e(TAG, "Invalid input uri")
            throw IllegalArgumentException("Invalid input uri")
        }
        val resolver = appContext.contentResolver
        val picture = BitmapFactory.decodeStream(
            resolver.openInputStream(Uri.parse(resourceUri))
        )
        val blurred = blurBitmap(picture, appContext)

        val outputUri = writeBitmapToFile(appContext, blurred)
        val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())
        return outputData
    }

    override fun doWork(): Result {
        return try {
            makeStatusNotification("Blurring image", applicationContext)
            val data = blur()
            sleep()
            Log.d(TAG, "doWork: ${data.getString(KEY_IMAGE_URI)}")
            Result.success(data)
        } catch (e: Throwable) {
            makeStatusNotification("Image Blurred and Saved", applicationContext)
            Log.e(TAG, "Error applying blur")
            return Result.failure()
        }
    }
}