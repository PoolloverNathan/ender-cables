package poollovernathan.fabric.endcables

import com.google.gson.JsonElement
import net.minecraft.util.Identifier
import java.util.function.Supplier

fun model(id: Identifier, builder: ModelBuilder.() -> Unit): JsonElement = ModelBuilder().also(builder).get()

@DslMarker
annotation class ModelDslMarker

@ModelDslMarker
class ModelBuilder: Supplier<JsonElement> {
    private val textures = mutableMapOf<String, Identifier>()
    override fun get(): JsonElement = JsonBuilder {
        obj("textures") {
            for ((key, value) in textures) {
                put(key, value.toString())
            }
        }
        ary("elements") {

        }
    }.element
}
