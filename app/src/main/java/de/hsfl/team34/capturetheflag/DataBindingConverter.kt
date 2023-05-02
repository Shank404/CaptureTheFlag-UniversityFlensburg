package de.hsfl.team34.capturetheflag

import androidx.databinding.InverseMethod

object DataBindingConverter{

    @InverseMethod("toInt")
    fun toString(value: Int): String {
        var parsedInt : String = ""
        value.let {
            parsedInt = it.toString()
        }
        return parsedInt
    }


    fun toInt(value: String): Int {
        var parsedString : Int = Int.MAX_VALUE
        value.let {
            parsedString = it.toInt()
        }
        return parsedString
    }

}
