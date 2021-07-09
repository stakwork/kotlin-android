package io.matthewnelson.crypto_common.clazzes

import java.io.CharArrayWriter
import java.security.SecureRandom

class PasswordGenerator(passwordLength: Int, chars: Set<Char> = DEFAULT_CHARS) {

    companion object {
        const val MIN_PASSWORD_LENGTH = 12
        const val MIN_CHAR_POOL_SIZE = 30


        val DEFAULT_CHARS: Set<Char> by lazy {
            val numbers = NUMBERS
            val lettersLower = A_TO_Z_LOWER
            val lettersUpper = A_TO_Z_UPPER

            val set: MutableSet<Char> = LinkedHashSet(numbers.size + lettersLower.size + lettersUpper.size)

            set.addAll(numbers + lettersLower + lettersUpper)
            set.toSet()
        }

        @Suppress("MemberVisibilityCanBePrivate")
        val NUMBERS: Set<Char>
            get() = setOf(
                '0', '1', '2', '3', '4',
                '5', '6', '7', '8', '9',
            )

        @Suppress("MemberVisibilityCanBePrivate")
        val A_TO_Z_LOWER: Set<Char>
            get() = setOf(
                'a', 'b', 'c', 'd', 'e',
                'f', 'g', 'h', 'i', 'j',
                'k', 'l', 'm', 'n', 'o',
                'p', 'q', 'r', 's', 't',
                'u', 'v', 'w', 'x', 'y',
                'z',
            )

        @Suppress("MemberVisibilityCanBePrivate")
        val A_TO_Z_UPPER: Set<Char>
            get() = setOf(
                'A', 'B', 'C', 'D', 'E',
                'F', 'G', 'H', 'I', 'J',
                'K', 'L', 'M', 'N', 'O',
                'P', 'Q', 'R', 'S', 'T',
                'U', 'V', 'W', 'X', 'Y',
                'Z',
            )
    }

    init {
        require(passwordLength >= MIN_PASSWORD_LENGTH) {
            "passwordLength must be greater than or equal to $MIN_PASSWORD_LENGTH"
        }
        require(chars.size >= MIN_CHAR_POOL_SIZE) {
            "chars must contain greater than or equal to $MIN_CHAR_POOL_SIZE"
        }
    }

    val password: Password = SecureRandom().let { random ->
        CharArrayWriter(passwordLength).let { writer ->
            repeat(passwordLength) {
                writer.append(chars.elementAt(random.nextInt(chars.size)))
            }

            Password(writer.toCharArray()).also {
                writer.reset()
                repeat(passwordLength) {
                    writer.append('0')
                }
            }
        }
    }
}
