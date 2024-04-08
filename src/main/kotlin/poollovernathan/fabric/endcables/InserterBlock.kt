package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.MapColor
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties.FACING
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import poollovernathan.fabric.endcables.ExampleMod.id
import java.util.*

private val BlockState.inputFace; get() = this[FACING]
private val BlockState.outputFace; get() = inputFace.opposite

object InserterBlock: Block(netherite(MapColor.BLUE).nonOpaque()), HasID, Registerable, BlockEntityProvider {
    override val id = id("inserter")

    override fun register() {
        Registry.BLOCK += InserterBlock
        Registry.ITEM += BlockItem(InserterBlock, FabricItemSettings().group(ItemGroup.REDSTONE)) to id
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? = super.getPlacementState(ctx)?.with(FACING, if (ctx.player?.isSneaking == true) ctx.playerLookDirection else ctx.side.opposite)

    override fun registerDatagen(generator: FabricDataGenerator) {
        generator.language {
            add(InserterBlock, "Inserter")
        }
        generator.models {
            blockstate {
                registerSouthDefaultFacing(InserterBlock)
            }
            item {
                /* TODO */
            }
        }
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = InserterBlockEntity(pos, state)

    override fun <T: BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? = {
        BlockEntityTicker<InserterBlockEntity> { world, pos, state, blockEntity -> blockEntity.cooldown.dec() }
    }.takeIf { type == InserterBlockEntity.type }?.invoke()?.coerce()
}

class InserterBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(type, pos, state), CableTransferPacket.Sender, SidedInventory {
    val cooldown = Cooldown(10u)
    companion object: Registerable, HasID by InserterBlock {
        val type = FabricBlockEntityTypeBuilder.create({ pos, state -> InserterBlockEntity(pos, state) }, InserterBlock).build()
        override fun register() {
            Registry.BLOCK_ENTITY_TYPE += type to id
        }
    }

    override fun clear() = Unit

    override fun size() = 1

    override fun isEmpty() = true

    override fun getStack(slot: Int) = ItemStack.EMPTY

    override fun removeStack(slot: Int, amount: Int) = ItemStack.EMPTY

    override fun removeStack(slot: Int) = ItemStack.EMPTY

    override fun setStack(slot: Int, stack: ItemStack) {
        newTransaction { t ->
            if (slot == 0 && !stack.isEmpty) {
                cooldown.resetOrThrow()
                if (!ItemPacket(stack).send(cachedState.outputFace, t)) throw IllegalStateException("Failed to send packet")
                t.commit()
            } else {
                throw IllegalArgumentException("Square placed in circle hole")
            }
        }
    }

    override fun canPlayerUse(player: PlayerEntity?) = false

    override fun getAvailableSlots(side: Direction?) = if (side == cachedState.inputFace) intArrayOf(0) else intArrayOf()

    override fun canInsert(slot: Int, stack: ItemStack?, dir: Direction?): Boolean {
        if (!(slot == 0 && dir == cachedState.inputFace && cooldown.isReady && stack?.isEmpty?.not() == true)) return false
        newTransaction {
            return ItemPacket(stack).send(cachedState.outputFace, it)
        }
    }

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?) = false
}