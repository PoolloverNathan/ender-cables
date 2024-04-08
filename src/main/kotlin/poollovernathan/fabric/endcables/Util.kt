package poollovernathan.fabric.endcables

import com.google.gson.JsonObject
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.State
import net.minecraft.state.StateManager
import net.minecraft.state.property.Property
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3f
import net.minecraft.util.registry.Registry
import net.minecraft.world.WorldAccess
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.reflect.KProperty

inline infix fun Direction.Axis.towards(ad: Direction.AxisDirection): Direction = Direction.get(ad, this)
inline infix fun <T> T.into(consumer: Consumer<T>) {
    consumer.accept(this)
}

inline infix fun <T, U> Pair<T, U>.into(consumer: BiConsumer<T, U>) {
    consumer.accept(this.first, this.second)
}

inline operator fun <O, S : State<O, S>?> StateManager.Builder<O, S>.plusAssign(it: Property<*>) {
    add(it)
}

inline fun netherite(color: MapColor): FabricBlockSettings = FabricBlockSettings.of(Material.METAL, color).sounds(BlockSoundGroup.NETHERITE)

inline infix fun <A, B, C> Pair<A, B>.too(third: C) = Triple(first, second, third)
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

inline operator fun Item.times(count: Int) = ItemStack(this, Integer.max(count, this.maxCount))
class SetOnce<T> private constructor(private var value: T, private val limit: Boolean = false, private val fail: (init: Boolean) -> Nothing) {
    private object UNINIT

    private var wasSet = false

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
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (limit && wasSet) {
            fail(true)
        }
        this.value = value
    }
}

operator fun<T> Registry<T>.plusAssign(entry: T) {
    assert(entry is HasID) { "Entries registered using += without explicit ID must implement HasID. Either implement HasID on your entry or specify an explicit ID." }
    Registry.register(this, (entry as HasID).id, entry as T /* smartn't cast */)
}

operator fun<T> Registry<T>.plusAssign(entry: Pair<T, Identifier>) {
    Registry.register(this, entry.second, entry.first)
}

infix fun <T> Consumer<T>.gets(value: T) = accept(value)
infix fun <T, U> BiConsumer<T, U>.gets(value: Pair<T, U>) = accept(value.first, value.second)
infix fun <T, U> BiConsumer<T, U>.gets(first: T) = HalfConsumption(this, first)
class HalfConsumption<T, U> internal constructor(private val consumer: BiConsumer<T, U>, private val first: T) {
    infix fun to(second: U) {
        consumer.accept(first, second)
    }
}

@JvmInline
value class IDProvider(override val id: Identifier): HasID
class IDBlockEntityType<T: BlockEntity>(override val id: Identifier, vararg blocks: Block, factory: (pos: BlockPos, state: BlockState) -> T): BlockEntityType<T>(factory, setOf(*blocks), null),
    HasID, Registerable {
    override fun register() {
        Registry.BLOCK_ENTITY_TYPE += this
    }
}

operator fun NbtCompound.set(key: String, value: NbtElement) = put(key, value)
private fun LivingEntity.teleport(destination: Vec3d) {
    teleport(destination.x, destination.y, destination.z)
}

operator fun Vec3f.times(scale: Float) = Vec3f(x * scale, y * scale, z * scale)

@Suppress("UnstableApiUsage")
inline operator fun<R> TransactionContext?.invoke(block: (Transaction) -> R) = Transaction.openNested(this).use(block)

@Suppress("UnstableApiUsage")
inline fun<R> newTransaction(block: (Transaction) -> R) = null(block)

inline fun<R> stackOverflowFallback(value: R, block: () -> R) = try {
    block()
} catch (e: StackOverflowError) {
    value
}

inline fun<R> stackOverflowFallback(block: () -> R) = stackOverflowFallback(Unit, block)
fun WorldAccess.getStorage(pos: BlockPos, side: Direction): Storage<ItemVariant>? {
    val blockState = getBlockState(pos)
    val block = blockState.block
    val inv = if (block is InventoryProvider) {
        block.getInventory(blockState, this, pos)
    } else {
        getBlockEntity(pos) as? Inventory
    } ?: return null
    return InventoryStorage.of(inv, side)
}