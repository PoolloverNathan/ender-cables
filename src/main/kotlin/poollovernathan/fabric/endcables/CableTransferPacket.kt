package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtType
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException
import poollovernathan.fabric.endcables.ExampleMod.id
import poollovernathan.fabric.endcables.ExampleMod.logger
import java.lang.IllegalArgumentException
import java.lang.Integer.max
import java.util.*

abstract class CableTransferPacket(val type: CableTransferPacketType) {
    init {
        assert(this::class.isInstance(type.createPacket()))
    }
    companion object {
        val registry = FabricRegistryBuilder.createSimple(CableTransferPacketType::class.java, id("cable_transfer_packets")).buildAndRegister()
        fun fromNbt(nbt: NbtCompound): CableTransferPacket? {
            nbt.getString("Type")
                .runCatching { Identifier(this) }
                .map { registry.get(it) }
                .map { it ?: throw IllegalArgumentException("Identifier does not refer to an entry") }
                .map { it.createPacket() }
                .onSuccess { it.readNbt(nbt) }
                .getOrNull()
                .also { return it }
        }
    }
    fun toNbt(nbt: NbtCompound) {
        writeNbt(nbt)
        nbt.putString("Type", registry.getKey(type).toString())
    }
    abstract fun readNbt(nbt: NbtCompound)
    protected abstract fun writeNbt(nbt: NbtCompound)
    abstract fun getPulseTexture(): Identifier

    interface CableTransferPacketType {
        fun createPacket(): CableTransferPacket
    }
}

class ItemPacket: CableTransferPacket(ItemPacket) {
    companion object: CableTransferPacketType {
        override fun createPacket(): CableTransferPacket = ItemPacket()
    }
    var item = ItemStack.EMPTY
    override fun readNbt(nbt: NbtCompound) {
        item = (nbt.get("Item") as? NbtCompound)?.let { ItemStack.fromNbt(it) } ?: ItemStack.EMPTY
    }

    override fun writeNbt(nbt: NbtCompound) {
        NbtCompound().also {
            item.writeNbt(it)
            nbt.put("Item", it)
        }
    }

    override fun getPulseTexture(): Identifier = id("block/item_pulse.png")
}

operator fun Item.times(count: Int) = ItemStack(this, max(count, this.maxCount))