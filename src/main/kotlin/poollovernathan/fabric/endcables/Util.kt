package poollovernathan.fabric.endcables

import com.google.gson.JsonObject
import net.minecraft.state.State
import net.minecraft.state.StateManager
import net.minecraft.state.property.Property
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Supplier

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