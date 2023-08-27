package poollovernathan.fabric.endcables

import com.google.gson.JsonObject
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.state.State
import net.minecraft.state.StateManager
import net.minecraft.state.property.Property
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.reflect.KProperty

infix fun Direction.Axis.towards(ad: Direction.AxisDirection) = Direction.get(ad, this)
infix fun <T> T.into(consumer: Consumer<T>) {
    consumer.accept(this)
}

infix fun <T, U> Pair<T, U>.into(consumer: BiConsumer<T, U>) {
    consumer.accept(this.first, this.second)
}

operator fun <O, S : State<O, S>?> StateManager.Builder<O, S>.plusAssign(it: Property<*>) {
    add(it)
}

infix fun <A, B, C> Pair<A, B>.too(third: C) = Triple(first, second, third)
data class ModelTransform(
    val translation: Vec3d = Vec3d.ZERO, val rotation: Vec3d = Vec3d.ZERO, val scale: Vec3d = Vec3d(1.0, 1.0, 1.0)
) : Supplier<JsonObject> {
    override fun get() = JsonBuilder {
        if (translation != Vec3d.ZERO) ary("translation", translation.x, translation.y, translation.z)
        if (rotation != Vec3d.ZERO) ary("rotation", rotation.x, rotation.y, rotation.z)
        if (scale != Vec3d.ZERO) ary("scale", scale.x, scale.y, scale.z)
    }.element
}

enum class TransformationType {
    THIRDPERSON_LEFT, THIRDPERSON_RIGHT, FIRSTPERSON_LEFT, FIRSTPERSON_RIGHT, GROUND, GUI, FIXED
}

operator fun Item.times(count: Int) = ItemStack(this, Integer.max(count, this.maxCount))
class SetOnce<T> private constructor(private var value: T, private val limit: Boolean = false, private val fail: (init: Boolean) -> Nothing) {
    private object UNINIT;
    private var wasSet = false;
    @Suppress("UNCHECKED_CAST")
    companion object {
        fun<T> once(fail: (init: Boolean) -> Nothing) = SetOnce(UNINIT as T, true, fail)
        fun<T> many(failGet: () -> Nothing) = SetOnce(UNINIT as T, false) { failGet() }
        fun<T> default(default: T, failSet: () -> Nothing) = SetOnce(default, true) { failSet() }
    }
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (value === UNINIT) {
            fail(false)
        }
        return value;
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (limit && wasSet) {
            fail(true)
        }
        this.value = value
    }
}