package com.xiaopo.flying.sticker

import android.graphics.Matrix
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

class ObservableMatrix : BaseObservable() {
    private val _matrix = Matrix()

    @Bindable
    fun getMatrix(): Matrix {
        return _matrix
    }

    fun setMatrix(matrix: Matrix) {
        _matrix.set(matrix)
        notifyPropertyChanged(BR.matrix)
    }

    fun setValues(floats: FloatArray) {
        _matrix.setValues(floats)
        notifyPropertyChanged(BR.matrix)
    }

    fun invert(a: Matrix) {
        _matrix.invert(a)
    }
}