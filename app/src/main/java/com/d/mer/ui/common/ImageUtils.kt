package com.d.mer.ui.common

import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.d.mer.R
import java.io.OutputStream


object ImageUtils {

    fun getImageUriIfPresent(activity: Activity, imageNodeAddress: String): Uri? {
        try {
            val imageName = "$imageNodeAddress.png"
            val resolver = activity.contentResolver

            val projection =
                arrayOf(MediaStore.Images.Media.DISPLAY_NAME, MediaStore.MediaColumns._ID)
            val selection = MediaStore.Files.FileColumns.DISPLAY_NAME + " like ?"
            val selectionArgs = arrayOf("%$imageName%")

            val cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )

            cursor?.let { cur ->
                if (cur.moveToNext()) {
                    val id = cur.getLong(cur.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    return ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                }
            }
            cursor?.close()
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.info("Exception: $e")
            activity.runOnUiThread {
                Dialogs.showMessage(activity, "ImageUriIfPresent\n$e")
            }
            return null
        }
    }

    fun saveImage(bitmap: Bitmap, activity: Activity, imageNodeAddress: String): Uri? {
        try {
            val imageName = "$imageNodeAddress.png"
            val fos: OutputStream?
            val imageUri: Uri?
            val resolver = activity.contentResolver

            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "DCIM/${activity.getString(R.string.app_name)}"
                )
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            imageUri = resolver.insert(collection, contentValues)
            fos = resolver.openOutputStream(imageUri!!)

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos?.let {
                it.flush()
                it.close()
            }
            return imageUri
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.info("Exception: $e")
            activity.runOnUiThread {
                Dialogs.showMessage(activity, "SaveImage\n$e")
            }
            return null
        }
    }

}