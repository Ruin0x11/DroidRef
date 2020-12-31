package com.xiaopo.flying.sticker.extensions

import android.graphics.Paint
import android.graphics.Typeface
import com.xiaopo.flying.sticker.TextSticker

/**
 * Developed by
 * @author Elad Finish
 */

const val UNDERLINE = Paint.UNDERLINE_TEXT_FLAG
const val STRIKE_THROUGH = Paint.STRIKE_THRU_TEXT_FLAG


fun TextSticker.withTypefaceBoldItalic(bold: Boolean, italic: Boolean) {
    val typeface: Typeface? = textPaint.typeface
    val style = getTypefaceBoldItalic(bold, italic)
    textPaint.typeface = Typeface.create(typeface, style)
}

fun TextSticker.withUnderline(selected: Boolean) {
    val flags = textPaint.flags
    if (selected) {
        textPaint.flags = flags or UNDERLINE
    } else {
        textPaint.flags = flags xor UNDERLINE
    }
}

fun TextSticker.withStrikethrough(selected: Boolean) {
    val flags: Int = textPaint.flags
    if (selected) {
        textPaint.flags = flags or STRIKE_THROUGH
    } else {
        textPaint.flags = flags xor STRIKE_THROUGH
    }
}

private fun getTypefaceBoldItalic(bold: Boolean, italic: Boolean): Int {
    var typeface = Typeface.NORMAL
    if (bold && italic) {
        typeface = Typeface.BOLD_ITALIC
    } else if (bold) {
        typeface = Typeface.BOLD
    } else if (italic) {
        typeface = Typeface.ITALIC
    }
    return typeface
}
