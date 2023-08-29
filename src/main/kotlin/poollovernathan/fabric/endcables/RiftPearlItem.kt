package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.data.client.Models
import net.minecraft.data.server.RecipeProvider
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.Items.ENDER_PEARL
import net.minecraft.item.Items.GLOWSTONE_DUST
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtInt
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import poollovernathan.fabric.endcables.ExampleMod.id
import java.util.Optional


object RiftPearlItem: Item(Settings().group(ItemGroup.REDSTONE).maxCount(1)), Registerable, HasID {
    override val id = id("rift_pearl")

    override fun register() {
        Registry.register(Registry.ITEM, id, this)
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
        var nbt = stack.nbt?.getList("Target", NbtInt.INT_TYPE.toInt()).takeIf { (it?.size ?: 0) == 3 } ?: return null;
        return BlockPos(nbt.getInt(0), nbt.getInt(1), nbt.getInt(2))
    }
}