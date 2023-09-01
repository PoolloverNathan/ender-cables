package poollovernathan.fabric.endcables

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.util.function.Supplier

@DslMarker
private annotation class JsonDslMarker

@JsonDslMarker
class JsonBuilder constructor(): Supplier<JsonElement> {
    constructor(builder: JsonBuilder.() -> Unit): this() {
        builder(this)
    }
    val element = JsonObject()
    fun put(key: String, value: JsonElement?) = element.add(key, value)
    fun put(key: String, value: Char) = element.addProperty(key, value)
    fun put(key: String, value: Number) = element.addProperty(key, value)
    fun put(key: String, value: String) = element.addProperty(key, value)
    fun put(key: String, value: Boolean) = element.addProperty(key, value)
    fun nil(key: String) = put(key, null)
    fun ary(key: String): JsonArrayBuilder {
        val ary = JsonArrayBuilder()
        element.add(key, ary.element)
        return ary
    }
    inline fun ary(key: String, builder: JsonArrayBuilder.() -> Unit) = builder(ary(key))
    fun ary(key: String, vararg elements: JsonElement) = ary(key) {
        elements.forEach {
            put(it)
        }
    }
    fun ary(key: String, vararg elements: Char) = ary(key) {
        elements.forEach {
            put(it)
        }
    }
    fun ary(key: String, vararg elements: Number) = ary(key) {
        elements.forEach {
            put(it)
        }
    }
    fun ary(key: String, vararg elements: String) = ary(key) {
        elements.forEach {
            put(it)
        }
    }
    fun ary(key: String, vararg elements: Boolean) = ary(key) {
        elements.forEach {
            put(it)
        }
    }
    fun obj(key: String): JsonBuilder {
        val obj = JsonBuilder()
        element.add(key, obj.element)
        return obj
    }
    inline fun obj(key: String, builder: JsonBuilder.() -> Unit) = builder(obj(key))

    override fun get() = element
}

@JsonDslMarker
class JsonArrayBuilder constructor(): Supplier<JsonElement> {
    constructor(builder: JsonArrayBuilder.() -> Unit): this() {
        builder(this)
    }
    fun put(value: JsonElement?) = element.add(value)
    fun put(value: Char) = element.add(value)
    fun put(value: Number) = element.add(value)
    fun put(value: String) = element.add(value)
    fun put(value: Boolean) = element.add(value)
    fun nil() = put(null)
    fun ary(): JsonArrayBuilder {
        val ary = JsonArrayBuilder()
        element.add(ary.element)
        return ary
    }
    fun ary(builder: JsonArrayBuilder.() -> Unit) = builder(ary())
    fun ary(vararg elements: JsonElement) = ary {
        elements.forEach {
            put(it)
        }
    }
    fun ary(vararg elements: Char) = ary {
        elements.forEach {
            put(it)
        }
    }
    fun ary(vararg elements: Number) = ary {
        elements.forEach {
            put(it)
        }
    }
    fun ary(vararg elements: String) = ary {
        elements.forEach {
            put(it)
        }
    }
    fun ary(vararg elements: Boolean) = ary {
        elements.forEach {
            put(it)
        }
    }
    fun obj(): JsonBuilder {
        val obj = JsonBuilder()
        element.add(obj.element)
        return obj
    }
    fun obj(builder: JsonBuilder.() -> Unit) = builder(obj())

    override fun get() = element

    val element = JsonArray()
}