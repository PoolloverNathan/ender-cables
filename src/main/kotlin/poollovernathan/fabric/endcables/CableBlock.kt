package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.MapColor
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.data.client.BlockStateModelGenerator
import net.minecraft.data.client.ItemModelGenerator
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import poollovernathan.fabric.endcables.ExampleMod.id
import java.util.function.Consumer

object CableBlock :
    Block(Settings.of(Material.METAL, MapColor.DARK_GREEN).requiresTool().strength(10.0f, 120.0f).sounds(BlockSoundGroup.NETHERITE)),
    Registerable, HasID {
    override val id = id("cable")

    override fun register() {
        Registry.register(Registry.BLOCK, id, CableBlock)
    }


    override fun registerDatagen(generator: FabricDataGenerator) {
        generator.addProvider(object : FabricModelProvider(generator) {
            override fun generateBlockStateModels(blockStateModelGenerator: BlockStateModelGenerator?) {
                val modelId = id("block/cable")
                blockStateModelGenerator!!.blockStateCollector.accept(BlockStateModelGenerator.createAxisRotatedBlockState(CableBlock, modelId))
                blockStateModelGenerator.modelCollector.accept(modelId, JsonBuilder {
                    obj("textures") {
                        put("side", id("block/cable_sides.png").toString())
                        put("top", id("block/misc0.png").toString())
                        put("particle", "#side")
                    }
                    ary("elements") {
                        fun produce(highX: Boolean, highZ: Boolean) = obj {
                            ary("from", if (highX) 11 else 4, 0, if (highZ) 11 else 4)
                            ary("to", if (highX) 12 else 5, 16, if (highZ) 12 else 5)
                            val offset = (if (highX) 4 else 0) + (if (highZ) 8 else 0)
                            obj("faces") {
                                for (dir in DIRECTIONS) obj (dir.getName()) {
                                    if (dir.axis == Direction.Axis.Y) {
                                        val shift = (offset / 4)
                                        ary("uv", shift, dir.id, shift + 1, dir.id + 1)
                                        put("texture", "#top")
                                    } else {
                                        val shift = offset + dir.id - 2
                                        ary("uv", shift, 0, shift + 1, 16)
                                        put("texture", "#side")
                                    }
                                }
                            }
                        }
                        produce(highX = false, highZ = false)
                        produce(highX = true, highZ = false)
                        produce(highX = false, highZ = true)
                        produce(highX = true, highZ = true)
                    }
                })
            }

            override fun generateItemModels(itemModelGenerator: ItemModelGenerator?) {

            }
        })
        generator.addProvider(object: FabricRecipeProvider(generator) {
            override fun generateRecipes(exporter: Consumer<RecipeJsonProvider>?) {
                TODO("Not yet implemented")
            }

        })
    }
}

class CableEntity(pos: BlockPos?, state: BlockState?) : BlockEntity(TYPE, pos, state) {
    companion object: Registerable, HasID by CableBlock {
        val TYPE: BlockEntityType<CableEntity> = FabricBlockEntityTypeBuilder.create(::CableEntity, CableBlock).build()
        override fun register() {
            Registry.register(Registry.BLOCK_ENTITY_TYPE, id, TYPE)
        }
    }
}