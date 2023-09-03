package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.MapColor
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.data.client.*
import net.minecraft.data.client.VariantSettings.Rotation
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.state.property.Properties.FACING
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import poollovernathan.fabric.endcables.ExampleMod.id

object ConsumerBlock: Block(netherite(MapColor.ORANGE)), HasID, Registerable, BlockEntityProvider {
    override val id = id("consumer")

    override fun register() {
        Registry.BLOCK += ConsumerBlock
        Registry.ITEM += BlockItem(ConsumerBlock, FabricItemSettings().group(ItemGroup.REDSTONE)) to id
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? = super.getPlacementState(ctx)?.with(FACING, if (ctx.player?.isSneaking == true) ctx.playerLookDirection else ctx.side.opposite)

    override fun registerDatagen(generator: FabricDataGenerator) {
        generator.language {
            add(ConsumerBlock, "Consumer")
        }
        generator.models {
            blockstate {
                registerSouthDefaultFacing(ConsumerBlock)
            }
            item {
                /* TODO */
            }
        }
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = ConsumerBlockEntity(pos, state)

    override fun <T: BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? = {
        BlockEntityTicker<ConsumerBlockEntity> { world, pos, state, blockEntity -> blockEntity.cooldown.dec() }
    }.takeIf { type == ConsumerBlockEntity.type }?.invoke()?.coerce()
}

fun BlockStateModelGenerator.registerSouthDefaultFacing(block: Block) {
    blockStateCollector gets VariantsBlockStateSupplier
        .create(block, BlockStateVariant.create().put(VariantSettings.MODEL, ModelIds.getBlockModelId(block)))
        .coordinate(createSouthDefaultRotationStates())
}

fun createSouthDefaultRotationStates() = BlockStateVariantMap.create(Properties.FACING).register(
    Direction.SOUTH, BlockStateVariant.create()
).register(Direction.WEST, BlockStateVariant.create().put(VariantSettings.Y, Rotation.R90)).register(
    Direction.NORTH, BlockStateVariant.create().put(VariantSettings.Y, Rotation.R180)
).register(Direction.EAST, BlockStateVariant.create().put(VariantSettings.Y, Rotation.R270)).register(Direction.DOWN, BlockStateVariant.create().put(VariantSettings.X, Rotation.R90)).register(Direction.UP, BlockStateVariant.create().put(VariantSettings.X, Rotation.R270))

class ConsumerBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(type, pos, state), CableTransferPacket.Handler {
    val cooldown = Cooldown(5u)
    companion object: Registerable, HasID by ConsumerBlock {
        val type = FabricBlockEntityTypeBuilder.create({ pos, state -> ConsumerBlockEntity(pos, state) }, ConsumerBlock).build()
        override fun register() {
            Registry.BLOCK_ENTITY_TYPE += type to id
        }
    }

    override fun canReceivePacket(packet: CableTransferPacket, direction: Direction, transaction: Transaction): Boolean {
        if (!cooldown.isReady) return false
        if (direction != cachedState[FACING]) return false
        val stack = if (packet is ItemPacket) packet.item.also { if(it.isEmpty) return true } else return false
        val output = world?.getStorage(pos.add(direction.opposite.vector), direction) ?: return false
        return output.insert(ItemVariant.of(packet.item), packet.item.count asa Long, transaction) == packet.item.count asa Long
    }

    override fun receivePacket(packet: CableTransferPacket, direction: Direction, transaction: Transaction) {
        if (direction != cachedState[FACING]) throw IllegalArgumentException("Cannot accept packets from this direction")
        val stack = if (packet is ItemPacket) packet.item.also { if(it.isEmpty) return } else throw IllegalArgumentException("Cannot accept packets that are not ItemPackets")
        val output = world?.getStorage(pos.add(direction.opposite.vector), direction) ?: throw IllegalArgumentException("Consumer does not have a storage in the proper direction")
        transaction { t ->
            if (output.insert(ItemVariant.of(packet.item), packet.item.count asa Long, t) == packet.item.count asa Long) {
                cooldown.resetOrThrow()
                t.commit()
            } else {
                throw IllegalArgumentException("Insertion not accepted by storage, disregard above error")
            }
        }
    }
}