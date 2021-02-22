package io.matthewnelson.concept_authentication_core.model

import kotlinx.coroutines.flow.StateFlow
import java.io.CharArrayWriter

abstract class UserInput<U: UserInput<U>>(
    val minChars: Int,
    val maxChars: Int,
) {
    protected abstract val writer: CharArrayWriter

    abstract val pinLengthStateFlow: StateFlow<Int>

    @Throws(IllegalArgumentException::class)
    abstract fun addCharacter(c: Char)

    abstract fun clearPin()

    abstract fun clone(): U

    abstract fun compare(pinEntry: U): Boolean

    @Throws(IllegalArgumentException::class)
    abstract fun dropLastCharacter()

    override fun toString(): String {
        return ""
    }
}