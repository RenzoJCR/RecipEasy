package com.recipeasy.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {

    const val STORAGE_PERMISSION_REQUEST_CODE = 1001

    fun checkStoragePermission(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - READ_MEDIA_IMAGES para fotos/videos
            ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-12 - No necesita permiso para guardar en Downloads
            true
        } else {
            // Android 9 o inferior - WRITE_EXTERNAL_STORAGE
            ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Android 9 o inferior
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
        // Android 10-12 no necesita pedir permiso
    }

    fun shouldShowRequestPermissionRationale(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                android.Manifest.permission.READ_MEDIA_IMAGES
            )
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            false
        }
    }
}