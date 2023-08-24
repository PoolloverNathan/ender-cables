package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3f
import net.minecraft.world.World
import poollovernathan.fabric.endcables.ExampleMod.id
import java.lang.Integer.max
import kotlin.IllegalArgumentException

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
    abstract fun getColor(): Int
    open fun onDie(world: World, pos: Vec3d, dir: Direction?) = Unit


    interface CableTransferPacketType {
        fun createPacket(): CableTransferPacket
    }

    interface Handler {
        fun getWorld(): World?
        fun getPos(): BlockPos
        /**
         * Recieves a packet from another Handler.
         * @param packet The packet that was recieved.
         * @param direction The direction the packet is coming from.
         * @return A {@link Result} with no value. If this resolves successfully, the sender should forget about the packet. Failures can usually be ignored. Usually.
         */
        fun recievePacket(packet: CableTransferPacket, direction: Direction): Result<Unit>

        /**
         * A convenience function to send a packet to another Handler.
         * @param packet The packet to send.
         * @param direction The direction to send the packet in.
         * @return A {@link Result} with no value. This either contains success if {@link #recievePacket} returned success,
         */
        fun sendPacket(packet: CableTransferPacket, direction: Direction) = runCatching {
            val targetPos = getPos().offset(direction)
            val target = getWorld()?.getBlockEntity(targetPos) ?: throw MissingHandlerError(targetPos)
            if (target !is Handler) throw InvalidHandlerError(targetPos, target)
            target.recievePacket(packet, direction.opposite)
                .getOrElse { throw PacketRefusedError(targetPos, target, it) }
        }
        /**
         * An alias for {@link #sendPacket}.
         */
        fun CableTransferPacket.send(direction: Direction) = sendPacket(this, direction)
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

    override fun getColor() = 0xffffff

    override fun onDie(world: World, pos: Vec3d, dir: Direction?) {
        var ejectVelocity = Vec3d(dir?.unitVector ?: Vec3f.ZERO)
        if (dir != Direction.DOWN) ejectVelocity = ejectVelocity.add(Vec3d(0.0, 1.0, 0.0))
        ejectVelocity = ejectVelocity.multiply(0.1)
        val entity = ItemEntity(world, pos.x, pos.y, pos.z, item, ejectVelocity.x, ejectVelocity.y, ejectVelocity.z)
        world.spawnEntity(entity)
    }
}

sealed class PacketDeliveryError(val target: BlockPos): Error()
sealed class PacketDestinationError(target: BlockPos) :
    PacketDeliveryError(target)
class MissingHandlerError(target: BlockPos): PacketDestinationError(target)
class InvalidHandlerError(target: BlockPos, val handler: BlockEntity): PacketDestinationError(target)
class PacketRefusedError(target: BlockPos, val handler: BlockEntity, cause: Throwable): PacketDeliveryError(target) {
    init {
        initCause(cause)
    }
}

operator fun Item.times(count: Int) = ItemStack(this, max(count, this.maxCount))