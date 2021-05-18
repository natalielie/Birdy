package org.tokend.muna.util.validator

interface CharSequenceValidator {
    fun isValid(sequence: CharSequence?): Boolean
}