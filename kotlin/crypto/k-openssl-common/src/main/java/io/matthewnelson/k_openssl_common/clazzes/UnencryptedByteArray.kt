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
package io.matthewnelson.k_openssl_common.clazzes

import io.matthewnelson.k_openssl_common.annotations.UnencryptedDataAccess
import io.matthewnelson.k_openssl_common.extensions.toCharArray

inline class UnencryptedByteArray(@property: UnencryptedDataAccess val value: ByteArray) {

    @OptIn(UnencryptedDataAccess::class)
    fun clear() {
        value.fill('*'.toByte())
    }

    @Throws(IllegalAccessException::class)
    override fun toString(): String {
        throw IllegalAccessException("toUnencryptedString must be used.")
    }

    @OptIn(UnencryptedDataAccess::class)
    fun toUnencryptedCharArray(): UnencryptedCharArray =
        UnencryptedCharArray(value.toCharArray())

    @OptIn(UnencryptedDataAccess::class)
    fun toUnencryptedString(): UnencryptedString =
        UnencryptedString(value.toString(Charsets.UTF_8).trim())
}