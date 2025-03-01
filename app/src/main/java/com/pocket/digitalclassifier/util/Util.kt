package com.pocket.digitalclassifier.util

import android.graphics.Bitmap
import android.graphics.Picture

object Util {
     fun createBitmapFromPicture(picture: Picture): Bitmap {
        val bitmap = Bitmap.createBitmap(
            picture.width,
            picture.height,
            Bitmap.Config.ARGB_8888
        )
         return bitmap
    }


}