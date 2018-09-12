package com.crestron.aurora.views

import android.content.Context
import android.preference.EditTextPreference
import android.util.AttributeSet
import com.crestron.aurora.Loged

class FloatEditTextPreference : EditTextPreference {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun getPersistedString(defaultReturnValue: String): String {
        //return "${getPersistedFloat(1f)}"
        return super.getPersistedString(defaultReturnValue)
    }

    override fun getPersistedBoolean(defaultReturnValue: Boolean): Boolean {
        Loged.i("$defaultReturnValue")
        return super.getPersistedBoolean(defaultReturnValue)
    }

    override fun getPersistedFloat(defaultReturnValue: Float): Float {
        Loged.i("$defaultReturnValue")
        return super.getPersistedFloat(defaultReturnValue)
    }

    override fun getPersistedInt(defaultReturnValue: Int): Int {
        Loged.i("$defaultReturnValue")
        return super.getPersistedInt(defaultReturnValue)
    }

    override fun getPersistedLong(defaultReturnValue: Long): Long {
        Loged.i("$defaultReturnValue")
        return super.getPersistedLong(defaultReturnValue)
    }

    override fun persistBoolean(value: Boolean): Boolean {
        Loged.i("$value")
        return super.persistBoolean(value)
    }

    override fun persistFloat(value: Float): Boolean {
        Loged.i("$value")
        return super.persistFloat(value)
    }

    override fun persistInt(value: Int): Boolean {
        Loged.i("$value")
        return super.persistInt(value)
    }

    override fun persistLong(value: Long): Boolean {
        Loged.i("$value")
        return super.persistLong(value)
    }

    override fun persistString(value: String): Boolean {
        Loged.i("$value")
        //return persistFloat(value.toFloat())
        return super.persistString(value)
    }
}