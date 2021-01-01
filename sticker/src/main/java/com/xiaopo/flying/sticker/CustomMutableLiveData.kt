package com.xiaopo.flying.sticker

import androidx.databinding.BaseObservable
import androidx.databinding.Observable
import androidx.lifecycle.MutableLiveData

class CustomMutableLiveData<T : BaseObservable?>(value: T) : MutableLiveData<T>() {

    override fun setValue(value: T) {
        super.setValue(value)

        //listen to property changes
        value!!.addOnPropertyChangedCallback(callback)
    }

    var callback: Observable.OnPropertyChangedCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {

            //Trigger LiveData observer on change of any property in object
            setValue(value!!)
        }
    }

    init {
        setValue(value)
    }
}