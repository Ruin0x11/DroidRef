package com.xiaopo.flying.sticker

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.google.gson.*
import timber.log.Timber
import java.io.*
import java.lang.reflect.Type
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


class StickerViewSerializer {
    private final val METADATA_FILE = "droidref_metadata.json"

    private class MatrixSerializer : JsonSerializer<Matrix> {
        override fun serialize(src: Matrix, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val floats = FloatArray(9)
            src.getValues(floats)
            var array = JsonArray(9)
            for (i in 0 until 9) {
                array.add(JsonPrimitive(floats[i]))
            }
            return array
        }
    }

    private class MatrixDeserializer : JsonDeserializer<Matrix> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Matrix {
            val floats = FloatArray(9)
            for (i in 0 until 9) {
                floats[i] = json.asJsonArray[i].asFloat
            }
            val mat = Matrix()
            mat.setValues(floats)
            return mat
        }
    }

    private fun getGson(): Gson =
        GsonBuilder().registerTypeAdapter(Matrix::class.java, MatrixSerializer()).registerTypeAdapter(Matrix::class.java, MatrixDeserializer()).create()

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        var bitmap: Bitmap? = null
        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable
            if (bitmapDrawable.bitmap != null) {
                return bitmapDrawable.bitmap
            }
        }
        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun sha256(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    data class Entry(val sha256: String, val bounds: Rect, val matrix: Matrix, val cropBounds: RectF, val flipHorizontal: Boolean, val flipVertical: Boolean)
    data class Metadata(val date: Long, val entries: MutableMap<String, Entry>)

    fun serialize(view: StickerView, file: File) {
        val zipStream = ZipOutputStream(BufferedOutputStream(FileOutputStream(file)))

        val entries: MutableMap<String, Entry> = HashMap()

        view.stickers.forEach {
            val drawableSticker = it as DrawableSticker
            val bitmap = drawableToBitmap(drawableSticker.drawable)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            val sha = sha256(byteArray)
            val entry = Entry(
                sha,
                drawableSticker.getRealBounds(),
                drawableSticker.matrix,
                drawableSticker.croppedBounds,
                drawableSticker.isFlippedHorizontally,
                drawableSticker.isFlippedVertically
            )
            val filename = "$sha.png"
            entries[filename] = entry
            zipStream.putNextEntry(ZipEntry(filename))
            zipStream.write(byteArray)
        }

        zipStream.putNextEntry(ZipEntry(METADATA_FILE))
        zipStream.write(getGson().toJson(Metadata(System.currentTimeMillis(), entries)).toByteArray(Charsets.UTF_8))
        zipStream.close()
    }

    fun isValidFile(file: File): Boolean {
        return ZipFile(file).getEntry(METADATA_FILE) != null
    }

    fun deserialize(view: StickerView, stream: InputStream, resources: Resources): Boolean {
//        if (!isValidFile(file)) {
//            return false
//        }

        val zipStream = ZipInputStream(BufferedInputStream(stream))

        view.removeAllStickers()
        view.resetView()

        val bitmapArrays: MutableMap<String, ByteArray> = HashMap()
        var metadata: Metadata? = null

        while (true) {
            val entry: ZipEntry = zipStream.nextEntry ?: break
            val fileName = entry.name
            val streamBuilder = ByteArrayOutputStream()
            var bytesRead: Int
            val tempBuffer = ByteArray(8192 * 2)
            while (zipStream.read(tempBuffer).also { bytesRead = it } != -1) {
                streamBuilder.write(tempBuffer, 0, bytesRead)
            }
            val bytes = streamBuilder.toByteArray()
            if (fileName == METADATA_FILE) {
                metadata = getGson().fromJson(String(bytes, Charsets.UTF_8), Metadata::class.java)
            } else {
                bitmapArrays[fileName] = bytes
            }
            streamBuilder.close()
        }

        bitmapArrays.forEach { (t, u) ->
            val entry = metadata!!.entries[t]!!
            val bitmap = BitmapFactory.decodeByteArray(u, 0, u.size)
            val drawable = BitmapDrawable(resources, bitmap)
            val sticker = DrawableSticker(drawable)
            sticker.realBounds = entry.bounds
            sticker.croppedBounds = entry.cropBounds
            sticker.setMatrix(entry.matrix)
            sticker.isFlippedHorizontally = entry.flipHorizontal
            sticker.isFlippedVertically = entry.flipVertical
            view.addStickerRaw(sticker)
        }

        zipStream.close()

        return true
    }
}