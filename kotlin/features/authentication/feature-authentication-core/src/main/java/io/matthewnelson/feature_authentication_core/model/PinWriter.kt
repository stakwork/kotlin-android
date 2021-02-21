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

import io.matthewnelson.k_openssl_common.annotations.RawPasswordAccess
import io.matthewnelson.k_openssl_common.clazzes.Password
import io.matthewnelson.k_openssl_common.clazzes.clear
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.CharArrayWriter

class PinWriter private constructor(): CharArrayWriter(MAX_CHAR_COUNT) {

    companion object {
        @JvmSynthetic
        internal fun instantiate(): PinWriter =
            PinWriter()

        const val MIN_CHAR_COUNT: Int = 8
        const val MAX_CHAR_COUNT: Int = 42
    }

    @Suppress("RemoveExplicitTypeArguments", "PropertyName")
    private val _pinLengthStateFlow: MutableStateFlow<Int> by lazy {
        MutableStateFlow<Int>(0)
    }

    val pinLengthStateFlow: StateFlow<Int>
        get() = _pinLengthStateFlow.asStateFlow()

    @Synchronized
    @Throws(IllegalArgumentException::class)
    fun addCharacter(c: Char) {
        if (this.size() + 1 > MAX_CHAR_COUNT) {
            throw IllegalArgumentException(
                "Cannot add anymore characters"
            )
        }

        this.append(c)
        _pinLengthStateFlow.value += 1
    }

    @Synchronized
    fun clearPin() {
        buf.fill('*')
        this.reset()
        _pinLengthStateFlow.value = 0
    }

    @Synchronized
    fun clone(): PinWriter =
        PinWriter().also { newWriter ->
            for (i in 0 until this.size()) {
                newWriter.addCharacter(buf[i])
            }
        }

    @Synchronized
    @OptIn(RawPasswordAccess::class)
    fun compare(pinWriter: PinWriter): Boolean {
        if (this.hashCode() == pinWriter.hashCode()) {
            return true
        }

        if (this.size() != pinWriter.size()) {
            return false
        }

        var pinsMatch = true
        Password(pinWriter.toCharArray()).let { copy ->
            for (i in 0 until this.size()) {
                if (buf[i] != copy.value[i]) {
                    pinsMatch = false
                    break
                }
            }
            copy.clear()
        }

        return pinsMatch
    }

    @Synchronized
    @Throws(IllegalArgumentException::class)
    fun drop(n: Int = 1) {
        require(n > 0) { "n must be greater than 0" }

        if (count == 0) {
            throw IllegalArgumentException("index cannot be less than 0")
        }

        if (count - n >= 0) {
            count -= n
            _pinLengthStateFlow.value -= n
        } else {
            reset()
            _pinLengthStateFlow.value = 0
        }
    }

    override fun toString(): String {
        return ""
    }
}
