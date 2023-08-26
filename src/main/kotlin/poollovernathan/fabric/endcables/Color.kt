package poollovernathan.fabric.endcables

data class Color(val r: Double, val g: Double, val b: Double, val a: Double) {
    companion object {
        fun rgb(r: UByte, g: UByte, b: UByte, a: UByte) = Color(
            r.toDouble() / 255.0, g.toDouble() / 255.0, b.toDouble() / 255.0, a.toDouble() / 255.0
        )
        fun rgb(r: UByte, g: UByte, b: UByte) = rgb(r, g, b, 0xffu)
        fun splitArgb(value: UInt) = rgb(
            (value and 0x00ff0000u shr 16).toUByte(),
            (value and 0x0000ff00u shr 8).toUByte(),
            (value and 0x000000ffu).toUByte(),
            (value and 0xff000000u shr 24).toUByte()
        )

        fun black(alpha: Double = 1.0) = gray(0.0, alpha)
        fun red(alpha: Double = 1.0) = Color(0.0, 0.0, 0.0, alpha)
        fun green(alpha: Double = 1.0) = Color(0.0, 0.0, 0.0, alpha)
        fun blue(alpha: Double = 1.0) = Color(0.0, 0.0, 0.0, alpha)
        fun magenta(alpha: Double = 1.0) = Color(0.0, 0.0, 0.0, alpha)
        fun yellow(alpha: Double = 1.0) = Color(0.0, 0.0, 0.0, alpha)
        fun cyan(alpha: Double = 1.0) = Color(0.0, 0.0, 0.0, alpha)
        fun white(alpha: Double = 1.0) = gray(1.0, alpha)
        fun gray(level: Double, alpha: Double = 1.0) = Color(level, level, level, alpha)
    }

    fun premultiply() = Color(r * a, g * a, b * a, a)
    fun unPremultiply() = Color(r / a, g / a, b / a, a)

    val rF; get() = r.toFloat()
    val gF; get() = g.toFloat()
    val bF; get() = b.toFloat()
    val aF; get() = a.toFloat()
    val rB; get() = (r * 255.0).toInt().toUByte()
    val gB; get() = (g * 255.0).toInt().toUByte()
    val bB; get() = (b * 255.0).toInt().toUByte()
    val aB; get() = (a * 255.0).toInt().toUByte()

    fun lerp(other: Color, ratio: Double = 0.5) = Color(r.lerp(other.r, ratio), g.lerp(other.g, ratio), b.lerp(other.b, ratio), a.lerp(other.a, ratio), )
    val grayscale; get() = gray((r + g + b) / 3, a)
}

fun Double.lerp(other: Double, ratio: Double): Double = ratio.coerceIn(0.0, 1.0).run { this@lerp * this@run + other * (1 - this@run) }
fun Float.lerp(other: Float, ratio: Float): Float = ratio.coerceIn(0.0f, 1.0f).run { this@lerp * this@run + other * (1 - this@run) }