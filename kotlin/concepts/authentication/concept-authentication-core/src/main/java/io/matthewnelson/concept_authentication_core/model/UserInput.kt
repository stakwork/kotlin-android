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