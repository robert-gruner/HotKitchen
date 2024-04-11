package hotkitchen.common

fun String.isEmailValid(): Boolean {
    val emailRegex = ("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
    return emailRegex.toRegex().matches(this)
}

fun String.isPasswordValid(): Boolean {
    return this.length > 5 && this.any { it.isDigit() } && this.any { it.isLetter() }
}
