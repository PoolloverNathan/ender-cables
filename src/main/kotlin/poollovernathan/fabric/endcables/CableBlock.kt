package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.data.client.BlockStateModelGenerator.createAxisRotatedBlockState
import net.minecraft.data.server.RecipeProvider
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.item.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import poollovernathan.fabric.endcables.ExampleMod.id
import java.util.*

object CableBlock : Block(
    Settings.of(Material.METAL, MapColor.DARK_GREEN).requiresTool().strength(10.0f, 120.0f)
        .sounds(BlockSoundGroup.NETHERITE).nonOpaque()
), Registerable, HasID, BlockEntityProvider {
    init {
        defaultState = defaultState.with(Properties.AXIS, Direction.Axis.Y)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOutlineShape(state: BlockState?, world: BlockView?, pos: BlockPos?, context: ShapeContext?): VoxelShape =
        ((0.25 to 0.75) to (0.0 to 1.0)).run {
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
        generator.models {
            val modelId = id("block/cable")
            fun createModel(item: Boolean = false) = JsonBuilder {
                obj("textures") {
                    put("cable", id("block/cable").toString())
                    put("misc", id("block/misc0").toString())
                    put("particle", "#cable")
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
                            ary("from", 11, 15.999, 11)
                            ary("to", 5, 0.001, 5)
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

            blockstate {
                createAxisRotatedBlockState(CableBlock, modelId) into blockStateCollector
                modelId to createModel(false) into modelCollector
            }

            item {
                id.run { Identifier(namespace, "item/$path") } to createModel(true) into writer
            }
        }
        generator.recipes {
            ShapedRecipeJsonBuilder.create(CableBlock, 24)
                .criterion("ender_pearl", RecipeProvider.conditionsFromItem(Items.ENDER_PEARL))
                .input('g', Items.GOLD_INGOT).input('e', Items.ENDER_PEARL).pattern("geg").pattern("   ")
                .pattern("geg").offerTo(it)
        }
        generator.language {
            add(CableBlock, "Ender Cable")
        }
    }

    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): BlockEntity? {
        return CableEntity(pos ?: return null, state ?: return null)
    }
}

private val BlockState.dir0
    get() = this[Properties.AXIS] towards Direction.AxisDirection.NEGATIVE
private val BlockState.dir1
    get() = this[Properties.AXIS] towards Direction.AxisDirection.POSITIVE

class CableEntity(pos: BlockPos?, state: BlockState?) : BlockEntity(type, pos, state), CableTransferPacket.Handler {
    var packets = PacketsContainer(1u)
    var forward = TransactionalStorage(true)
    var insertionTime = TransactionalStorage(0L)
    companion object : Registerable, HasID by CableBlock {
        val type: BlockEntityType<CableEntity> = FabricBlockEntityTypeBuilder.create(::CableEntity, CableBlock).build()
        override fun register() {
            Registry.register(Registry.BLOCK_ENTITY_TYPE, id, type)
        }
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        newTransaction { t ->
            packets.readNbt(nbt, "Packets", t)
            forward[t] = nbt?.getBoolean("Negative")?.not() ?: true
            insertionTime[t] = nbt?.getLong("InsertionTime")?.takeIf { it != 0L } ?: world?.time ?: 0L
            @Suppress("UnstableApiUsage") t.commit()
        }
    }

    override fun writeNbt(nbt: NbtCompound) {
        packets.writeNbt(nbt, "Packets")
        nbt?.putBoolean("Negative", !forward.value)
        nbt?.putLong("InsertionTime", insertionTime.value)
        super.writeNbt(nbt)
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener?> = BlockEntityUpdateS2CPacket.create(this)

    override fun toInitialChunkDataNbt(): NbtCompound? = createNbt()



    override fun receivePacket(packet: CableTransferPacket, direction: Direction, @Suppress("UnstableApiUsage") transaction: Transaction) = run {
        transaction {
            val packetStorage = this.packets.packets[0]
            if (packetStorage.value.isPresent) return@transaction false
            forward[it] = when (direction) {
                cachedState.dir0 -> true
                cachedState.dir1 -> false
                else             -> return@transaction false
            }
            insertionTime[it] = world?.time ?: 0L
            packetStorage[it] = Optional.of(packet)
            it.commit()
            true
        }
    }
}