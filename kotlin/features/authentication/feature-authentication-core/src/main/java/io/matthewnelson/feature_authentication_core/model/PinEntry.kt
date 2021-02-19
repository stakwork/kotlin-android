package io.matthewnelson.feature_authentication_core.model

import kotlinx.coroutines.flow.StateFlow

inline class PinEntry(private val value: PinWriter = PinWriter.instantiate()) {

    @JvmSynthetic
    internal fun getPinWriter(): PinWriter =
        value

    val pinLengthStateFlow: StateFlow<Int>
        get() = value.pinLengthStateFlow

    @Throws(IllegalArgumentException::class)
    fun addCharacter(c: Char) {
        value.addCharacter(c)
    }

    fun clearPin() {
        value.clearPin()
    }

    fun clone(): PinEntry =
        PinEntry(value.clone())

    fun compare(pinEntry: PinEntry): Boolean =
        value.compare(pinEntry.value)

    @Throws(IllegalArgumentException::class)
    fun dropLastCharacter() {
        value.drop(1)
    }

    override fun toString(): String {
        return ""
    }
}
