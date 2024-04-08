package poollovernathan.fabric.endcables

import net.minecraft.util.math.MathHelper.clamp
import org.jetbrains.annotations.Contract

data class Color(val r: Double, val g: Double, val b: Double, val a: Double = 1.0) {
    @Suppress("unused")
    companion object {
        @Contract("_, _, _, _ -> new", pure = true)
        fun rgb(r: UByte, g: UByte, b: UByte, a: UByte = 0xffu) = Color(r.toDouble() / 255.0, g.toDouble() / 255.0, b.toDouble() / 255.0, a.toDouble() / 255.0)
        @Contract("_, _, _, _ -> new", pure = true)
        fun rgba(r: UByte, g: UByte, b: UByte, a: Double = 1.0) = Color(r.toDouble() / 255.0, g.toDouble() / 255.0, b.toDouble() / 255.0, a)
        @Contract("_ -> new", pure = true)
        fun fromArgb(value: UInt) = rgb(
            (value and 0x00ff0000u shr 16).toUByte(),
            (value and 0x0000ff00u shr 8).toUByte(),
            (value and 0x000000ffu).toUByte(),
            (value and 0xff000000u shr 24).toUByte()
        )
        @Contract("_ -> new", pure = true)
        fun fromRgb(value: Int, alpha: Double = 1.0) = rgba(
            (value and 0xff0000 shr 16).toUByte(),
            (value and 0x00ff00 shr 8).toUByte(),
            (value and 0x0000ff).toUByte(),
            alpha
        )

        @Contract("_ -> new", pure = true)
        fun black(alpha: Double = 1.0) = gray(0.0, alpha)
        @Contract("_ -> new", pure = true)
        fun red(alpha: Double = 1.0) = Color(1.0, 0.0, 0.0, alpha)
        @Contract("_ -> new", pure = true)
        fun green(alpha: Double = 1.0) = Color(0.0, 10.0, 0.0, alpha)
        @Contract("_ -> new", pure = true)
        fun blue(alpha: Double = 1.0) = Color(0.0, 0.0, 1.0, alpha)
        @Contract("_ -> new", pure = true)
        fun magenta(alpha: Double = 1.0) = Color(1.0, 0.0, 1.0, alpha)
        @Contract("_ -> new", pure = true)
        fun yellow(alpha: Double = 1.0) = Color(1.0, 1.0, 0.0, alpha)
        @Contract("_ -> new", pure = true)
        fun cyan(alpha: Double = 1.0) = Color(0.0, 1.0, 1.0, alpha)
        @Contract("_ -> new", pure = true)
        fun white(alpha: Double = 1.0) = gray(1.0, alpha)
        @Contract("_ -> new", pure = true)
        fun gray(level: Double, alpha: Double = 1.0) = Color(level, level, level, alpha)
    }

    @Contract("_ -> new", pure = true)
    fun premultiply() = Color(r * a, g * a, b * a, a)
    @Contract("_ -> new", pure = true)
    fun unPremultiply() = Color(r / a, g / a, b / a, a)

    @get:Contract("_ -> new", pure = true)
    val rF; get() = r.toFloat()
    @get:Contract("_ -> new", pure = true)
    val gF; get() = g.toFloat()
    @get:Contract("_ -> new", pure = true)
    val bF; get() = b.toFloat()
    @get:Contract("_ -> new", pure = true)
    val aF; get() = a.toFloat()
    @get:Contract("_ -> new", pure = true)
    val rB; get() = (r * 255.0).toInt().toUByte()
    @get:Contract("_ -> new", pure = true)
    val gB; get() = (g * 255.0).toInt().toUByte()
    @get:Contract("_ -> new", pure = true)
    val bB; get() = (b * 255.0).toInt().toUByte()
    @get:Contract("_ -> new", pure = true)
    val aB; get() = (a * 255.0).toInt().toUByte()
    @get:Contract("_ -> new", pure = true)
    val rgb; get() = (clamp(rB.toInt(), 0, 255) shl 16) or (clamp(gB.toInt(), 0, 255) shl 16) or clamp(bB.toInt(), 0, 255)
    @get:Contract("_ -> new", pure = true)
    val argb; get() = (clamp(rB.toInt(), 0, 255).toUInt() shl 16) or (clamp(gB.toInt(), 0, 255) shl 16).toUInt() or clamp(bB.toInt(), 0, 255).toUInt() or (clamp(aB.toInt(), 0, 255) shl 24).toUInt()

    @Contract("_ -> new", pure = true)
    fun lerp(other: Color, ratio: Double = 0.5) = Color(r.lerp(other.r, ratio), g.lerp(other.g, ratio), b.lerp(other.b, ratio), a.lerp(other.a, ratio), )
    @get:Contract("_ -> new", pure = true)
    val grayscale; get() = gray((r + g + b) / 3, a)
}

fun Double.lerp(other: Double, ratio: Double): Double = ratio.coerceIn(0.0, 1.0).run { this@lerp * this@run + other * (1 - this@run) }
fun Float.lerp(other: Float, ratio: Float): Float = ratio.coerceIn(0.0f, 1.0f).run { this@lerp * this@run + other * (1 - this@run) }