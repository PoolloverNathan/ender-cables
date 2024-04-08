package poollovernathan.fabric.endcables

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import java.util.*

class Cooldown(val limit: UInt, val name: String = "Cooldown") {
    val isReady get() = value == 0u
    var value: UInt = 0u

    /**
     * Runs the function if and only if the cooldown is at 0. If the function returns a present result, the limit will be reset.
     */
    inline operator fun<T: Any> invoke(function: () -> Optional<T>): Optional<T> {
        return if (value == 0u) {
            val result = function()
            if (result.isPresent) {
                value = limit
            }
            return result
        } else {
            Optional.empty()
        }
    }
    operator fun dec(): Cooldown {
        if (value != 0u) value--
        return this
    }
    fun readNbt(nbt: NbtCompound) {
        if (nbt.contains(name, NbtElement.INT_TYPE asan Int)) {
            value = nbt.getInt(name) asa UInt
        } else {
            value = 0u
        }
    }
    fun writeNbt(nbt: NbtCompound) {
        nbt.putInt(name, value asan Int)
    }
    fun reset() {
        value = limit
    }
    fun resetOrThrow() {
        if (value == 0u) {
            value = limit
        } else {
            throw IllegalStateException("$name not ready")
        }
    }
}