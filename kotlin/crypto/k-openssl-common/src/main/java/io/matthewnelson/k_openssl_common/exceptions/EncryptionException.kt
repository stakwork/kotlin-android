package io.matthewnelson.k_openssl_common.exceptions

class EncryptionException: Exception {
    constructor(cause: Throwable?) : super(cause) {}
    constructor(message: String?) : super(message) {}
    constructor(message: String?, cause: Throwable) : super(message, cause) {}
}
