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
package io.matthewnelson.test_feature_authentication_core

import io.matthewnelson.feature_authentication_core.AuthenticationCoreCoordinator
import io.matthewnelson.feature_authentication_core.components.AuthenticationManagerInitializer

open class TestAuthenticationCoreCoordinator<
        T: AuthenticationManagerInitializer,
        S: TestEncryptionKeyHandler,
        V: TestAuthenticationCoreStorage
        >(
    testManager: TestAuthenticationCoreManager<T, S, V>
): AuthenticationCoreCoordinator<T>(testManager)
{
    var navigationCalled = false
    override suspend fun navigateToAuthenticationView() {
        navigationCalled = true
    }
}