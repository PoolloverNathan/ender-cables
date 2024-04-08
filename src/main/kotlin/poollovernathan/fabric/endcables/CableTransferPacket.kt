package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import org.jetbrains.annotations.Contract
import poollovernathan.fabric.endcables.ExampleMod.id
import kotlin.jvm.optionals.getOrElse

abstract class CableTransferPacket(val type: CableTransferPacketType<*>) {
    fun getEjectVelocity(direction: Direction): Vec3d {
        var ejectVelocity = Vec3d(direction.unitVector)
        if (direction != Direction.DOWN) ejectVelocity = ejectVelocity.add(Vec3d(0.0, 1.0, 0.0))
        return ejectVelocity.multiply(0.1)
    }

    companion object {
        val registry = FabricRegistryBuilder.createSimple(CableTransferPacketType::class.java, id("cable_transfer_packets")).buildAndRegister()

        /**
         * Attempts to load a [CableTransferPacket] from [nbt].
         * @returns An instance of [CableTransferPacket], or `null` if [nbt] does not contain a `Type` key, if the key exists but is not a valid identifier, or if it is valid but does not refer to an entry of [registry].
         */
        fun fromNbt(nbt: NbtCompound): CableTransferPacket? = nbt.getString("Type")
            .runCatching { Identifier(this) }
            .mapCatching { registry.get(it) }
            .map { it?.createPacket() }
            .onSuccess { it?.readNbt(nbt) }
            .getOrNull()
    }

    /**
     * Converts a packet to NBT. This adds the `Type` tag, allowing [fromNbt] to read it and create the corresponding packet.
     */
    fun toNbt(nbt: NbtCompound) {
        writeNbt(nbt)
        registry.getKey(type).getOrElse {
            nbt.remove("Type")
            return
        }.also { nbt.putString("Type", it.value.toString()) }
    }

    /**
     * Reads any data a packet may have stored in NBT. This is called when [fromNbt] is called, which is usually called when a [Handler] loads.
     */
    abstract fun readNbt(nbt: NbtCompound)
    protected abstract fun writeNbt(nbt: NbtCompound)

    /**
     * Returns the color of the packet. Each packet type should have a differentiable color.
     */
    open fun getColor() = Color.white()

    /**
     * Called when the packet dies. When this is called, it should drop any contained resources it can.
     */
    open fun die(world: World, pos: Vec3d, dir: Direction, @Suppress("UnstableApiUsage") transaction: Transaction) = Unit


    interface CableTransferPacketType<T: CableTransferPacket> {
        /**
         * Creates a packet of the type.
         */
        fun createPacket(): T
    }

    interface Receiver {
        /**
         * Validates a receiver's ability to receive a packet. This method MUST be pure.
         * @param packet The packet that the [Sender] is trying to send.
         * @param direction The direction of the [Sender] trying to send the packet.
         * @param transaction A transaction that is doubly-nested and therefore can only be used for validation purposes.
         * @return Whether the receiver can accept the packet. If true, at least one immediate [receivePacket] MUST be successful and MUST NOT throw. If false, [receivePacket] SHOULD throw.
         */
        @Contract(pure = true)
        fun canReceivePacket(packet: CableTransferPacket, direction: Direction, transaction: Transaction): Boolean
        /**
         * Receives a packet from another Handler. At this point, it is too late to cancel receiving.
         * @param packet The packet that was received.
         * @param direction The direction the packet is coming from.
         */
        @Contract(mutates = "this")
        fun receivePacket(packet: CableTransferPacket, direction: Direction, @Suppress("UnstableApiUsage") transaction: Transaction)
    }

    interface Sender {
        fun getWorld(): World?
        fun getPos(): BlockPos
        /**
         * A convenience function to send a packet to an adjacent [Receiver]. Locates the [Receiver], then calls [Receiver.canReceivePacket] (returning false if it returns false) and [Receiver.receivePacket].
         * @param packet The packet to send.
         * @param direction The direction to send the packet in.
         * @return A [Boolean] that is true if the packet could be sent. If it is true, the sender should forget about the packet.
         */
        fun sendPacket(packet: CableTransferPacket, direction: Direction, @Suppress("UnstableApiUsage") transaction: Transaction): Boolean {
            val targetPos = getPos().offset(direction)
            val target = getWorld()?.getBlockEntity(targetPos) ?: return false
            if (target !is Receiver) return false
            transaction.openNested().use { it.openNested().use { target.canReceivePacket(packet, direction.opposite, it ) }}.also { if (!it) return it }
            try {
                target.receivePacket(packet, direction.opposite, transaction)
            } catch (e: Throwable) {
                throw RuntimeException("Bad receivePacket implementation", e)
            }
            return true
        }

        /**
         * A convenience function to send a packet to an adjacent [Receiver]. Locates the [Receiver] and calls [Receiver.canReceivePacket]
         * @param packet The packet to send.
         * @param direction The direction to send the packet in.
         * @return A [Boolean] that is true if the packet could be sent. If it is true, the sender should forget about the packet.
         */
        fun canSendPacket(packet: CableTransferPacket, direction: Direction, @Suppress("UnstableApiUsage") transaction: Transaction): Boolean {
            val targetPos = getPos().offset(direction)
            val target = getWorld()?.getBlockEntity(targetPos) ?: return false
            if (target !is Receiver) return false
            return transaction.openNested().use { it.openNested().use { target.canReceivePacket(packet, direction, it ) }}
        }
        /**
         * An alias for [sendPacket].
         */
        @Suppress("unused")
        fun CableTransferPacket.send(direction: Direction, @Suppress("UnstableApiUsage") transaction: Transaction) = sendPacket(this, direction, transaction)
    }

    interface Handler: Sender, Receiver
}

class ItemPacket(var item: ItemStack = ItemStack.EMPTY): CableTransferPacket(ItemPacket) {
    companion object: CableTransferPacketType<ItemPacket>, HasID, Registerable {
        override fun createPacket() = ItemPacket()
        override val id = id("item")

        override fun register() {
            Registry.register(registry, id, this)
        }
    }
    override fun readNbt(nbt: NbtCompound) {
        item = (nbt.get("Item") as? NbtCompound)?.let { ItemStack.fromNbt(it) } ?: ItemStack.EMPTY
    }

    override fun writeNbt(nbt: NbtCompound) {
        NbtCompound().also {
            item.writeNbt(it)
            nbt.put("Item", it)
        }
    }

    override fun getColor() = Color.rgb(52u, 161u, 235u)

    override fun die(world: World, pos: Vec3d, dir: Direction, @Suppress("UnstableApiUsage") transaction: Transaction) {
        val ejectVelocity = getEjectVelocity(dir)
        val entity = ItemEntity(world, pos.x, pos.y, pos.z, item, ejectVelocity.x, ejectVelocity.y, ejectVelocity.z)
        world.spawnEntity(entity)
    }
}

abstract class ClientSyncedBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState):
    BlockEntity(type, pos, state) {
    override fun toUpdatePacket(): BlockEntityUpdateS2CPacket = BlockEntityUpdateS2CPacket.create(this)
    override fun toInitialChunkDataNbt(): NbtCompound = createNbt()
}

