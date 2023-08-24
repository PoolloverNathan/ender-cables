package poollovernathan.fabric.endcables

import com.google.gson.JsonObject
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.data.client.BlockStateModelGenerator
import net.minecraft.data.client.ItemModelGenerator
import net.minecraft.data.client.Model
import net.minecraft.data.server.RecipeProvider
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.item.*
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.State
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.state.property.Property
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import poollovernathan.fabric.endcables.ExampleMod.id
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Supplier

object CableBlock : Block(
    Settings.of(Material.METAL, MapColor.DARK_GREEN).requiresTool().strength(10.0f, 120.0f)
        .sounds(BlockSoundGroup.NETHERITE).nonOpaque()
), Registerable, HasID {
    init {
        defaultState = defaultState.with(Properties.AXIS, Direction.Axis.Y)
    }

    override fun getOutlineShape(state: BlockState?, world: BlockView?, pos: BlockPos?, context: ShapeContext?) =
        ((0.25 to 0.75) to (0.0 to 1.1)).run {
            when (state?.get(Properties.AXIS)) {
                Direction.Axis.X -> second to first too first
                Direction.Axis.Y, null -> first to second too first
                Direction.Axis.Z -> first to first too second
            }
        }.run {
            VoxelShapes.cuboid(first.first, second.first, third.first, first.second, second.second, third.second)
        }

    override fun getPlacementState(ctx: ItemPlacementContext?) =
        super.getPlacementState(ctx)?.with(Properties.AXIS, ctx?.side?.axis)

    val item by lazy { BlockItem(this, Item.Settings().group(ItemGroup.REDSTONE)) }
    override val id = id("cable")

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder += Properties.AXIS
    }

    override fun register() {
        Registry.register(Registry.BLOCK, id, CableBlock)
        Registry.register(Registry.ITEM, id, item)
    }


    override fun registerDatagen(generator: FabricDataGenerator) {
        generator.addProvider(object : FabricModelProvider(generator) {
            val modelId = id("block/cable")
            fun createModel(item: Boolean = false) = JsonBuilder {
                obj("textures") {
                    put("cable", id("block/cable").toString())
                    put("misc", id("block/misc0").toString())
                    put("particle", "#side")
                }
                ary("elements") {
                    fun produce(highX: Boolean, highZ: Boolean) = obj {
                        ary("from", if (highX) 11 else 4, 0, if (highZ) 11 else 4)
                        ary("to", if (highX) 12 else 5, 16, if (highZ) 12 else 5)
                        val offset = (if (highX) 4 else 0) + (if (highZ) 8 else 0)
                        obj("faces") {
                            for (dir in DIRECTIONS) obj(dir.getName()) {
                                if (dir.axis == Direction.Axis.Y) {
                                    val shift = (offset / 4)
                                    ary("uv", shift, dir.id, shift + 1, dir.id + 1)
                                    put("texture", "#misc")
                                } else {
                                    val shift = offset + dir.id - 2
                                    ary("uv", shift, 0, shift + 1, 16)
                                    put("texture", "#cable")
                                }
                            }
                        }
                    }
                    produce(highX = false, highZ = false)
                    produce(highX = true, highZ = false)
                    produce(highX = false, highZ = true)
                    produce(highX = true, highZ = true)
                    if (item) {
                        obj {
                            ary("from", 4.001, 0.001, 4.001)
                            ary("to", 11.999, 15.999, 11.999)
                            obj("faces") {
                                for (dir in DIRECTIONS) {
                                    obj(dir.getName()) {
                                        ary("uv", 8, 15, 9, 16)
                                        put("texture", "#misc")
                                    }
                                }
                            }
                        }
                    }
                }
                if (item) {
                    obj("display") {
                        for (type in enumValues<TransformationType>()) {
                            val transform = when (type) {
                                TransformationType.THIRDPERSON_LEFT, TransformationType.THIRDPERSON_RIGHT -> ModelTransform(
                                    rotation = Vec3d(75.0, 45.0, 90.0),
                                    translation = Vec3d(0.0, 2.5, 0.0),
                                    scale = Vec3d(0.375, 0.375, 0.375)
                                )

                                TransformationType.FIRSTPERSON_LEFT -> ModelTransform(
                                    rotation = Vec3d(0.0, 45.0, 0.0), scale = Vec3d(0.4, 0.4, 0.4)
                                )

                                TransformationType.FIRSTPERSON_RIGHT -> ModelTransform(
                                    rotation = Vec3d(0.0, 225.0, 0.0), scale = Vec3d(0.4, 0.4, 0.4)
                                )

                                TransformationType.GROUND -> ModelTransform(
                                    translation = Vec3d(0.0, 3.0, 0.0), scale = Vec3d(0.25, 0.25, 0.25)
                                )

                                TransformationType.GUI -> ModelTransform(
                                    rotation = Vec3d(30.0, 225.0, 90.0), scale = Vec3d(0.625, 0.625, 0.625)
                                )

                                TransformationType.FIXED -> ModelTransform(
                                    scale = Vec3d(0.5, 0.5, 0.5)
                                )
                            }
                            put(type.name.lowercase(), transform.get())
                        }
                    }
                }
            }

            override fun generateBlockStateModels(blockStateModelGenerator: BlockStateModelGenerator?) {
                if (blockStateModelGenerator == null) {
                    throw NullPointerException("BlockStateModelGenerator was null")
                }
                BlockStateModelGenerator.createAxisRotatedBlockState(
                    CableBlock, modelId
                ) into blockStateModelGenerator.blockStateCollector
                modelId to createModel(false) into blockStateModelGenerator.modelCollector
            }

            override fun generateItemModels(itemModelGenerator: ItemModelGenerator?) {
                id.run { Identifier(namespace, "item/$path") } to createModel(true) into itemModelGenerator!!.writer
            }
        })
        generator.addProvider(object : FabricRecipeProvider(generator) {
            override fun generateRecipes(exporter: Consumer<RecipeJsonProvider>) {
                ShapedRecipeJsonBuilder.create(CableBlock, 24)
                    .criterion("ender_pearl", RecipeProvider.conditionsFromItem(Items.ENDER_PEARL))
                    .input('g', Items.GOLD_INGOT).input('e', Items.ENDER_PEARL).pattern("geg").pattern("   ")
                    .pattern("geg").offerTo(exporter)
            }

        })
    }
}

data class ModelTransform(
    val translation: Vec3d = Vec3d.ZERO, val rotation: Vec3d = Vec3d.ZERO, val scale: Vec3d = Vec3d(1.0, 1.0, 1.0)
) : Supplier<JsonObject> {
    override fun get() = JsonBuilder {
        if (translation != Vec3d.ZERO) ary("translation", translation.x, translation.y, translation.z)
        if (rotation != Vec3d.ZERO) ary("rotation", rotation.x, rotation.y, rotation.z)
        if (scale != Vec3d.ZERO) ary("scale", scale.x, scale.y, scale.z)
    }.element
}

enum class TransformationType {
    THIRDPERSON_LEFT, THIRDPERSON_RIGHT, FIRSTPERSON_LEFT, FIRSTPERSON_RIGHT, GROUND, GUI, FIXED
}

infix fun <T> T.into(consumer: Consumer<T>) {
    consumer.accept(this)
}

infix fun <T, U> Pair<T, U>.into(consumer: BiConsumer<T, U>) {
    consumer.accept(this.first, this.second)
}

operator fun <O, S : State<O, S>?> StateManager.Builder<O, S>.plusAssign(it: Property<*>) {
    add(it)
}

infix fun <A, B, C> Pair<A, B>.too(third: C) = Triple(first, second, third)

class CableEntity(pos: BlockPos?, state: BlockState?) : BlockEntity(TYPE, pos, state) {
    companion object : Registerable, HasID by CableBlock {
        val TYPE: BlockEntityType<CableEntity> = FabricBlockEntityTypeBuilder.create(::CableEntity, CableBlock).build()
        override fun register() {
            Registry.register(Registry.BLOCK_ENTITY_TYPE, id, TYPE)
        }
    }
}