package org.tokend.muna.util.validator

/**
 * Validator of password strength
 */
object PasswordValidator :
    RegexValidator("^.{6,}$")