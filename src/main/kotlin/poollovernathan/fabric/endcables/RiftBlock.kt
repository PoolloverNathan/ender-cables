package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.MapColor
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.data.client.VariantsBlockStateSupplier
import net.minecraft.data.server.RecipeProvider
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import poollovernathan.fabric.endcables.ExampleMod.id

object RiftBlock: Block(netherite(MapColor.DARK_AQUA)), HasID, Registerable, BlockEntityProvider, BlockEntityTicker<RiftBlockEntity> {
    override val id = id("rift")

    override fun register() {
        Registry.BLOCK += RiftBlock
        RiftBlockEntity.register()
    }

    override fun registerDatagen(generator: FabricDataGenerator) {
        generator.models {
            blockstate {
                blockStateCollector.accept(VariantsBlockStateSupplier.create(RiftBlock))
            }
        }
        generator.recipes {
            it.shaped(RiftBlock) {
                criterion("tinted_glass", RecipeProvider.conditionsFromItem(ENDER_PEARL))
                pattern("dod")
                pattern("g g")
                pattern("dod")
                input('d', DIAMOND)
                input('o', OBSIDIAN)
                input('g', TINTED_GLASS)
            }
        }
    }

    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): BlockEntity? {
        return RiftBlockEntity(pos ?: return null, state ?: return null)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onUse(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        player: PlayerEntity?,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult {
        val entity = world?.getBlockEntity(pos) as? RiftBlockEntity ?: return ActionResult.PASS
        val handItem = player?.getStackInHand(hand) ?: ItemStack.EMPTY
        if (entity.pearl.isEmpty && handItem.item != RiftPearlItem) return ActionResult.PASS
        player?.giveItemStack(entity.pearl)
        entity.pearl = ItemStack.EMPTY
        if (!handItem.isEmpty && handItem.item == RiftPearlItem) {
            entity.pearl = handItem.split(1)
        }
        return ActionResult.SUCCESS;
    }

    override fun <T: BlockEntity> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? = this.coerce()

    override fun tick(world: World?, pos: BlockPos?, state: BlockState?, blockEntity: RiftBlockEntity?) {
        if ((blockEntity?.cooldown ?: 0u) > 0u) {
            blockEntity!!.cooldown--; // 0u > 0u cannot be true
            blockEntity.markDirty()
        }
    }
}

inline fun <reified T> Any.coerce() = this as? T
inline fun <T> T.elvis(fallback: () -> T) = this ?: fallback()
inline val <T> T.nonnull; get() = elvis { throw AssertionError("Non-null assertion failed") }

class RiftBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(type, pos, state), ProxyInventory {
    var pearl = ItemStack.EMPTY
        set(value) {
            if (value.isEmpty || pearl?.item == RiftPearlItem) {
                field = value
            } else {
                throw IllegalArgumentException("RiftBlockEntity#pearl can only be set to ItemStacks containing rift pearls")
            }
        }
    val target
        get() = RiftPearlItem.getTarget(pearl)
    override val targetInventory
        get() = target?.let { world?.getBlockEntity(it) }?.coerce<Inventory>() ?: DummyInventory

    var cooldown: UInt = 0u
    companion object: HasID by RiftBlock, Registerable {
        val type = IDBlockEntityType(id, RiftBlock, factory = ::RiftBlockEntity)
        override fun register() = type.register()
    }

    override fun readNbt(nbt: NbtCompound?) {
        nbt!!
        pearl = nbt.getCompound("Pearl").takeUnless { it.isEmpty }?.run(ItemStack::fromNbt)
        cooldown = nbt.getInt("Cooldown").toUInt()
    }

    override fun writeNbt(nbt: NbtCompound?) {
        nbt!!
        NbtCompound().takeUnless { pearl.isEmpty }?.also { pearl.writeNbt(it) }?.also { nbt.put("Pearl", it) }
        if (cooldown > 0u) nbt.putInt("Cooldown", cooldown.toInt())
    }

    override fun markDirty() {
        super<ProxyInventory>.markDirty()
        super<BlockEntity>.markDirty()
    }
}

