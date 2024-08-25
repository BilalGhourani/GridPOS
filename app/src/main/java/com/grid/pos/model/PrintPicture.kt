package com.grid.pos.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint

class PrintPicture private constructor() {

    var canvas: Canvas? = null // Canvas for drawing on the bitmap
    var paint: Paint? = null // Paint object to define how to draw
    var bm: Bitmap? = null // Bitmap to hold the image
    var width: Int = 0 // Width of the bitmap
    var length: Float = 0.0f // Length of the content drawn on the bitmap
    var bitbuf: ByteArray? = null // Buffer to hold bitmap data

    companion object {
        private val instance = PrintPicture()
        fun getInstance(): PrintPicture {
            return instance // Singleton instance of PrintPicture
        }
    }

    fun getLength(): Int {
        return length.toInt() + 20 // Returns the length with some padding
    }

    fun init(bitmap: Bitmap?) {
        if (bitmap != null) {
            initCanvas(bitmap.height) // Initialize canvas with bitmap dimensions
        }
        if (paint == null) {
            initPaint() // Initialize paint if not already done
        }
        if (bitmap != null) {
            drawImage(0f, 0f, bitmap) // Draw the bitmap on the canvas
        }
    }

    fun initCanvas(h: Int) {
        val w = h/2
        bm = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565) // Create a bitmap with specified width and height
        canvas = Canvas(bm!!) // Initialize the canvas with the bitmap
        canvas?.drawColor(-1) // Set the background color of the canvas to white
        width = w // Set the width
        bitbuf = ByteArray(width / 8) // Initialize bit buffer for raster image data
    }

    fun initPaint() {
        paint = Paint() // Create a new Paint object
        paint?.isAntiAlias = true // Enable anti-aliasing
        paint?.color = -16777216 // Set paint color to black
        paint?.style = Paint.Style.STROKE // Set paint style to stroke (only outlines)
    }

    /**
     * Draw bitmap on the canvas
     */
    fun drawImage(x: Float, y: Float, btm: Bitmap) {
        try {
            canvas?.drawBitmap(btm, x, y, null) // Draw the bitmap at the specified position
            if (length < y + btm.height)
                length = y + btm.height // Update the length if necessary
        } catch (e: Exception) {
            e.printStackTrace() // Handle any exceptions
        } finally {
            btm.recycle() // Recycle the bitmap to free up memory
        }
    }

    /**
     * Print using raster bitmap
     *
     * @return Byte array
     */
    fun printDraw(): ByteArray {
        return try {
            val nbm = Bitmap.createBitmap(bm!!, 0, 0, width, getLength()) // Create a new bitmap for printing

            val imgbuf = ByteArray(width / 8 * getLength() + 8) // Initialize the image buffer

            var s = 0

            // Print raster bitmap command
            imgbuf[0] = 29 // Hex 0x1D
            imgbuf[1] = 118 // Hex 0x76
            imgbuf[2] = 48 // 30
            imgbuf[3] = 0 // Bitmap mode 0,1,2,3
            // Representing horizontal bitmap byte count (xL+xH × 256)
            imgbuf[4] = (width / 8).toByte()
            imgbuf[5] = 0
            // Representing vertical bitmap dot count (yL + yH × 256)
            imgbuf[6] = (getLength() % 256).toByte()
            imgbuf[7] = (getLength() / 256).toByte()

            s = 7
            for (i in 0 until getLength()) { // Loop through the height of the bitmap
                for (k in 0 until width / 8) { // Loop through the width of the bitmap
                    val p0 = if (nbm.getPixel(k * 8 + 0, i) == -1) 0 else 1
                    val p1 = if (nbm.getPixel(k * 8 + 1, i) == -1) 0 else 1
                    val p2 = if (nbm.getPixel(k * 8 + 2, i) == -1) 0 else 1
                    val p3 = if (nbm.getPixel(k * 8 + 3, i) == -1) 0 else 1
                    val p4 = if (nbm.getPixel(k * 8 + 4, i) == -1) 0 else 1
                    val p5 = if (nbm.getPixel(k * 8 + 5, i) == -1) 0 else 1
                    val p6 = if (nbm.getPixel(k * 8 + 6, i) == -1) 0 else 1
                    val p7 = if (nbm.getPixel(k * 8 + 7, i) == -1) 0 else 1

                    val value = p0 * 128 + p1 * 64 + p2 * 32 + p3 * 16 + p4 * 8 + p5 * 4 + p6 * 2 + p7
                    bitbuf!![k] = value.toByte() // Set the corresponding byte in the bit buffer
                }

                for (t in 0 until width / 8) {
                    s++
                    imgbuf[s] = bitbuf!![t] // Copy the bit buffer to the image buffer
                }
            }

            bm?.recycle() // Recycle the bitmap to free up memory
            bm = null

            imgbuf // Return the image buffer containing the raster data
        } catch (e: Exception) {
            e.printStackTrace()
            ByteArray(0x0A) // Return an empty byte array if an error occurs
        }
    }
}
