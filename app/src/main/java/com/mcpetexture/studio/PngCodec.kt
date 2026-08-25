package com.mcpetexture.studio

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

object PngCodec {
    fun encode(state: PixelCanvasState): ByteArray {
        val bitmap = Bitmap.createBitmap(state.width, state.height, Bitmap.Config.ARGB_8888)
        for (y in 0 until state.height) {
            for (x in 0 until state.width) {
                bitmap.setPixel(x, y, state.pixels[y][x])
            }
        }
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        bitmap.recycle()
        return out.toByteArray()
    }

    fun decode(bitmap: Bitmap): PixelCanvasState {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = Array(h) { y ->
            IntArray(w) { x -> bitmap.getPixel(x, y) }
        }
        return PixelCanvasState(w, h, pixels)
    }
}
