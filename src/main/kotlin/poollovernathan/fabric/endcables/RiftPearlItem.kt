package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.data.client.Models
import net.minecraft.data.server.RecipeProvider
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder
import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.Items.ENDER_PEARL
import net.minecraft.item.Items.GLOWSTONE_DUST
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtList
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import poollovernathan.fabric.endcables.ExampleMod.id


object RiftPearlItem: Item(Settings().group(ItemGroup.REDSTONE).maxCount(1)), Registerable, HasID {
    override val id = id("rift_pearl")

    override fun register() {
        Registry.register(Registry.ITEM, id, this)
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        context.player ?: return ActionResult.PASS
        if (context.world.getBlockEntity(context.blockPos) is Inventory) {
            setTarget(context.stack, context.blockPos)
            return ActionResult.SUCCESS
        }
        return ActionResult.PASS
    }

    override fun registerDatagen(generator: FabricDataGenerator) {
        generator.recipes {
            ShapelessRecipeJsonBuilder.create(RiftPearlItem)
                .criterion("acquire_ender_pearl", RecipeProvider.conditionsFromItem(ENDER_PEARL))
                .input(ENDER_PEARL)
                .input(GLOWSTONE_DUST)
                .offerTo(it)
        }
        generator.language {
            add(RiftPearlItem, "Rift Pearl")
        }
        generator.models {
            item {
                register(RiftPearlItem, ENDER_PEARL, Models.GENERATED)
            }
        }
    }

    fun getTarget(stack: ItemStack): BlockPos? {
        if (stack.item != RiftPearlItem) return null;
        val nbt = stack.nbt?.getList("Target", NbtInt.INT_TYPE.toInt()).takeIf { (it?.size ?: 0) == 3 } ?: return null;
        return BlockPos(nbt.getInt(0), nbt.getInt(1), nbt.getInt(2))
    }
    fun setTarget(stack: ItemStack, target: BlockPos?) {
        if (stack.item != RiftPearlItem) return
        val nbt = stack.orCreateNbt
        if (target == null) {
            nbt.remove("Target")
        } else {
            nbt["Target"] = NbtList().also {
                it.add(NbtInt.of(target.x))
                it.add(NbtInt.of(target.y))
                it.add(NbtInt.of(target.z))
            }
        }
    }
}