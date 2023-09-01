package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.mixin.`object`.builder.AbstractBlockAccessor
import net.fabricmc.fabric.mixin.`object`.builder.AbstractBlockSettingsAccessor
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.data.client.*
import net.minecraft.data.server.RecipeProvider
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.Items.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.ItemScatterer
import net.minecraft.util.Rarity
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import poollovernathan.fabric.endcables.ExampleMod.id
import kotlin.jvm.optionals.getOrNull

@Suppress("UnstableApiUsage")
object RiftBlock: Block(netherite(MapColor.DARK_AQUA).nonOpaque().luminance(Blocks.NETHER_PORTAL.settings.luminance)), HasID, Registerable, BlockEntityProvider, BlockEntityTicker<RiftBlockEntity> {
    override val id = id("rift")

    override fun register() {
        Registry.BLOCK += RiftBlock
        Registry.ITEM += BlockItem(RiftBlock, Item.Settings().uncommon.group(ItemGroup.REDSTONE)) to id
        RiftBlockEntity.register()
    }

    override fun registerDatagen(generator: FabricDataGenerator) {
        generator.models {
            blockstate {
                blockStateCollector.accept(VariantsBlockStateSupplier.create(RiftBlock, BlockStateVariant().put(
                    VariantSettings.MODEL, blockModel)))
            }
            item {
                writer gets id to JsonBuilder {
                    put("parent", blockModel.toString())
                    Models.CUBE
                }
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
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        //if (world?.isClient == true) return ActionResult.PASS
        val entity = world.getBlockEntity(pos) as? RiftBlockEntity ?: return ActionResult.PASS
        val handItem = player.getStackInHand(hand) ?: ItemStack.EMPTY
        if (entity.pearl?.isEmpty != false && handItem.item != RiftPearlItem) return ActionResult.PASS
        entity.swapPearl(handItem) {
            if (!player.giveItemStack(it)) {
                ItemScatterer.spawn(world, pos, DefaultedList.ofSize(1, it))
            }
        }
        world.playSound(null, pos, if (entity.pearl?.isEmpty == false) SoundEvents.BLOCK_END_PORTAL_FRAME_FILL else SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.PLAYERS, 1f, 1f)
        return ActionResult.SUCCESS
    }

    override fun <T: BlockEntity> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? = this.coerce()

    override fun tick(world: World?, pos: BlockPos?, state: BlockState?, blockEntity: RiftBlockEntity?) {
        if ((blockEntity?.cooldown ?: 0u) > 0u) {
            blockEntity!!.cooldown-- // 0u > 0u cannot be true
            blockEntity.markDirty()
        }
    }

    override fun onStateReplaced(
        state: BlockState,
        world: World,
        pos: BlockPos,
        newState: BlockState,
        moved: Boolean
    ) {
        val entity = world.getBlockEntity(pos) as? RiftBlockEntity ?: return
        if (!moved) {
            entity.pearl?.also {
                ItemScatterer.spawn(world, pos.x asa Double, pos.y asa Double, pos.z asa Double, it)
            }
        }
    }
}

inline fun <reified T> Any.coerce() = this as? T
inline infix fun <T> T?.elvis(fallback: () -> T) = this ?: fallback()
private val HasID.itemModel: Identifier
    get() = Identifier(id.namespace, "item/${id.path}")
private val HasID.blockModel: Identifier
    get() = Identifier(id.namespace, "block/${id.path}")
inline val <T> T.nonnull; get() = elvis { throw AssertionError("Non-null assertion failed") }
infix fun ItemStack.equal(other: ItemStack) = ItemStack.areEqual(this, other)
inline val Item.Settings.common: Item.Settings; get() = rarity(Rarity.COMMON)
inline val Item.Settings.uncommon: Item.Settings; get() = rarity(Rarity.UNCOMMON)
inline val Item.Settings.rare: Item.Settings; get() = rarity(Rarity.RARE)
inline val Item.Settings.epic: Item.Settings; get() = rarity(Rarity.EPIC)


class RiftBlockEntity(pos: BlockPos, state: BlockState): ClientSyncedBlockEntity(type, pos, state), ProxyInventory {
    var pearl: ItemStack? = ItemStack.EMPTY
        private set
        get() = field ?: ItemStack.EMPTY
    fun setPearl(pearl: ItemStack?) = run {
        val mutPearl = pearl elvis { ItemStack.EMPTY }
        if (mutPearl.isEmpty || pearl?.item == RiftPearlItem) {
            this.pearl = pearl
            true
        } else {
            false
        }
    }

    fun takePearl(source: ItemStack?) {
        source ?: return
        val pearlSplit = source.split(1)
        if (!setPearl(pearlSplit)) {
            source.count++
        }
    }

    fun swapPearl(source: ItemStack?, oldConsumer: (ItemStack) -> Unit) {
        if (pearl == null || !pearl!!.isEmpty) oldConsumer(pearl!!)
        setPearl(ItemStack.EMPTY)
        takePearl(source)
        markDirty()
    }
    val target
        get() = RiftPearlItem.getTarget(pearl)
    override val targetInventory
        get() = target.getOrNull()?.let { world?.getBlockEntity(it) as? Inventory } ?: DummyInventory

    var cooldown: UInt = 0u
    companion object: HasID by RiftBlock, Registerable {
        val type = IDBlockEntityType(id, RiftBlock, factory = ::RiftBlockEntity)
        override fun register() = type.register()
    }

    override fun readNbt(nbt: NbtCompound?) {
        nbt!!
        pearl = nbt.getCompound("Pearl").takeUnless { it.isEmpty }?.run(ItemStack::fromNbt)
        cooldown = nbt.getInt("Cooldown") asa UInt
    }

    override fun writeNbt(nbt: NbtCompound?) {
        nbt!!
        NbtCompound().takeUnless { pearl?.isEmpty ?: true }?.also { pearl?.writeNbt(it) }?.also { nbt.put("Pearl", it) }
        if (cooldown > 0u) nbt.putInt("Cooldown", cooldown asan Int)
    }

    override fun markDirty() {
        super<ProxyInventory>.markDirty()
        super<ClientSyncedBlockEntity>.markDirty()
    }
}


inline val AbstractBlock.settings
    @Suppress("UnstableApiUsage")
    get() = (this as AbstractBlockAccessor).settings as AbstractBlockSettingsAccessor