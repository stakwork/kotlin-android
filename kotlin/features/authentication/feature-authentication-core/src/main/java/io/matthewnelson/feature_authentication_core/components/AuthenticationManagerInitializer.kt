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
package io.matthewnelson.feature_authentication_core.components

/**
 * Wrong Pin Lockout feature will be enabled **only** if both
 * [wrongPinAttemptsUntilLockedOut] and [wrongPinLockoutDuration] are
 * greater than 0.
 * */
abstract class AuthenticationManagerInitializer {
    abstract val wrongPinAttemptsUntilLockedOut: Int
    abstract val wrongPinLockoutDuration: Long
}
