package com.example.background.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlin.math.log

const val TAG = "Permissions"

class Permissions(private val ctx: Context) {
    var isReadPermissionGranted = false
    var isWritePermissionGranted = false
    private lateinit var _permissionResultLauncher: ActivityResultLauncher<Array<String>>
    var permissionResultLauncher: ActivityResultLauncher<Array<String>>
        get() = _permissionResultLauncher
        set(value) {
            _permissionResultLauncher = value
        }

    fun requestPermission() {
        val minSDK = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        isReadPermissionGranted = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isWritePermissionGranted = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isWritePermissionGranted = isWritePermissionGranted || minSDK

        val permissionRequest = mutableListOf<String>()

        if (!isReadPermissionGranted)
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!isWritePermissionGranted)
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissionRequest.isNotEmpty())
            permissionResultLauncher.launch(permissionRequest.toTypedArray())

    }
}