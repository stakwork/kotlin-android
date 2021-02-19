/*
*  Copyright 2021 Matthew Nelson
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
* */
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
