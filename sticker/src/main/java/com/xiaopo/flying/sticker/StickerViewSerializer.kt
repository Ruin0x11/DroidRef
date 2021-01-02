package com.xiaopo.flying.sticker

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import org.msgpack.core.MessagePack
import org.msgpack.core.MessagePacker
import org.msgpack.core.MessageUnpacker
import java.io.*
import java.security.MessageDigest

fun MessagePacker.packMatrix(matrix: Matrix) {
    val temp = FloatArray(9)
    matrix.getValues(temp)
    temp.forEach { this.packFloat(it) }
}
fun MessageUnpacker.unpackMatrix(): Matrix {
    val temp = FloatArray(9)
    (0..8).forEach{ temp[it] = this.unpackFloat() }
    val matrix = Matrix()
    matrix.setValues(temp)
    return matrix
}

fun MessagePacker.packRect(rect: Rect) {
    this.packInt(rect.left)
    this.packInt(rect.top)
    this.packInt(rect.right)
    this.packInt(rect.bottom)
}
fun MessageUnpacker.unpackRect(): Rect {
    val left = this.unpackInt()
    val top = this.unpackInt()
    val right = this.unpackInt()
    val bottom = this.unpackInt()
    return Rect(left, top, right, bottom)
}

fun MessagePacker.packRectF(rectF: RectF) {
    this.packFloat(rectF.left)
    this.packFloat(rectF.top)
    this.packFloat(rectF.right)
    this.packFloat(rectF.bottom)
}
fun MessageUnpacker.unpackRectF(): RectF {
    val left = this.unpackFloat()
    val top = this.unpackFloat()
    val right = this.unpackFloat()
    val bottom = this.unpackFloat()
    return RectF(left, top, right, bottom)
}

class StickerViewSerializer {
    data class Entry(
        val bounds: Rect,
        val matrix: Matrix,
        val cropBounds: RectF,
        val flipHorizontal: Boolean,
        val flipVertical: Boolean
    )
    data class StickerMetadata(val bitmap: ByteArray, val instances: MutableList<Entry>)
    data class Board(val date: Long, val stickers: List<StickerMetadata>)

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable
            if (bitmapDrawable.bitmap != null) {
                return bitmapDrawable.bitmap
            }
        }
        var bitmap: Bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
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

    private fun sha256(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    private fun serializeViewModel(viewModel: StickerViewModel): Board {
        val dedup: MutableMap<String, StickerMetadata> = mutableMapOf()

        viewModel.stickers.value!!.forEach {
            val drawableSticker = it as DrawableSticker
            val bitmap = drawableToBitmap(drawableSticker.drawable)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            val entry = Entry(
                drawableSticker.getRealBounds(),
                drawableSticker.matrix,
                drawableSticker.croppedBounds,
                drawableSticker.isFlippedHorizontally,
                drawableSticker.isFlippedVertically
            )
            val sha = sha256(byteArray)
            dedup.getOrPut(sha, { StickerMetadata(byteArray, mutableListOf()) }).instances.add(entry)
        }

        return Board(System.currentTimeMillis(), dedup.values.toList())
    }

    private fun deserializeViewModel(
        viewModel: StickerViewModel,
        board: Board,
        resources: Resources
    ) {
        viewModel.stickers.value!!.clear()
        board.stickers.flatMapTo(viewModel.stickers.value!!) { metadata ->
            metadata.instances.map { entry ->
                val bitmap = BitmapFactory.decodeByteArray(metadata.bitmap, 0, metadata.bitmap.size)
                val drawable = BitmapDrawable(resources, bitmap)
                val sticker = DrawableSticker(drawable)
                sticker.realBounds = entry.bounds
                sticker.croppedBounds = entry.cropBounds
                sticker.setMatrix(entry.matrix)
                sticker.isFlippedHorizontally = entry.flipHorizontal
                sticker.isFlippedVertically = entry.flipVertical
                sticker
            }
        }
    }

    fun serialize(viewModel: StickerViewModel, file: File) {
        val board = serializeViewModel(viewModel)

        // Why isn't there a maintained binary serialization library for Android? MoshiPack is
        // several Kotlin versions out of date, and Moshi is way too slow because it's JSON.
        MessagePack.newDefaultPacker(BufferedOutputStream(FileOutputStream(file)))
            .use { p ->
                p.packLong(board.date)
                p.packArrayHeader(board.stickers.size)
                board.stickers.forEach { sticker ->
                    p.packBinaryHeader(sticker.bitmap.size)
                    p.addPayload(sticker.bitmap)
                    p.packArrayHeader(sticker.instances.size)
                    sticker.instances.forEach {
                        p.packRect(it.bounds)
                        p.packMatrix(it.matrix)
                        p.packRectF(it.cropBounds)
                        p.packBoolean(it.flipHorizontal)
                        p.packBoolean(it.flipVertical)
                    }
                }
            }
    }

    fun deserialize(viewModel: StickerViewModel, inputStream: InputStream, resources: Resources) {
        val unpacked = MessagePack.newDefaultUnpacker(inputStream)
            .use { u ->
                val date = u.unpackLong()
                val stickerCount = u.unpackArrayHeader()
                val stickers: ArrayList<StickerMetadata> = ArrayList()
                (0 until stickerCount).mapTo(stickers) {
                    val bitmapSize = u.unpackBinaryHeader()
                    val bitmap = u.readPayload(bitmapSize)
                    val instanceCount = u.unpackArrayHeader()
                    val instances: ArrayList<Entry> = ArrayList()
                    (0 until instanceCount).mapTo(instances) {
                        val bounds = u.unpackRect()
                        val matrix = u.unpackMatrix()
                        val cropBounds = u.unpackRectF()
                        val flipHorizontal = u.unpackBoolean()
                        val flipVertical = u.unpackBoolean()
                        Entry(bounds, matrix, cropBounds, flipHorizontal, flipVertical)
                    }
                    StickerMetadata(bitmap, instances)
                }
                Board(date, stickers)
            }

        deserializeViewModel(viewModel, unpacked, resources)
    }
}