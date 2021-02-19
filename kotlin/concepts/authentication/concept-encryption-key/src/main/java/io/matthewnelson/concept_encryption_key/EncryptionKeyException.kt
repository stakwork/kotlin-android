package io.matthewnelson.concept_encryption_key

class EncryptionKeyException: Exception {
    constructor(cause: Throwable?) : super(cause) {}
    constructor(message: String?) : super(message) {}
    constructor(message: String?, cause: Throwable) : super(message, cause) {}
}
