package poollovernathan.fabric.endcables

import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.inventory.Inventory
import net.minecraft.util.math.Vec3f
import kotlin.jvm.optionals.getOrNull

class RiftRenderer private constructor(private val ctx: BlockEntityRendererFactory.Context?) :
    BlockEntityRenderer<RiftBlockEntity> {
    companion object: ClientRegisterable, HasID by RiftBlockEntity, BlockEntityRendererFactory<RiftBlockEntity> {
        override fun registerClient() {
            BlockEntityRendererFactories.register(RiftBlockEntity.type, this)
        }

        override fun create(ctx: BlockEntityRendererFactory.Context?) = RiftRenderer(ctx)

        val outline = ModelPart.Cuboid(0, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 6f, 6f, 6f, false, 0.0f, 0.0f)
    }

    override fun render(
        entity: RiftBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) = render(entity, tickDelta, matrices, vertexConsumers, light, overlay, 0u)

    fun render(
        entity: RiftBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int,
        stackDepth: UInt
    ) {
        entity.apply {
            matrices.apply {
                pearl?.takeUnless { it.isEmpty }?.also { item ->
                    push { entry ->
                        translate(0.5, 0.5, 0.5)
                        scale(-1.0f, -1.0f, -1.0f)
                        vertexConsumers.render(RenderLayer.getEndGateway()) { layer ->
                            outline.renderCuboid(entry, layer, 15, 15, 1f, 1f, 1f, 1f)
                        }
                    }
                    if (target.isPresent) {
                        world?.getBlockEntity(target.get())?.takeIf { it is Inventory }.also {
                            push { entry ->
                                val angle = (world?.time ?: 0) + tickDelta
                                translate(0.5, 0.5, 0.5)
                                scale(0.5f, 0.5f, 0.5f)
                                if (it != null && stackDepth < 512u) {
                                    multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(angle))
                                    translate(-0.5, -0.5, -0.5)
                                    if (it.type == RiftBlockEntity.type) {
                                        render(
                                            entity,
                                            tickDelta,
                                            matrices,
                                            vertexConsumers,
                                            light,
                                            overlay,
                                            stackDepth + 1u
                                        )
                                    } else {
                                        minecraft.blockEntityRenderDispatcher.render(
                                            it, tickDelta, matrices, vertexConsumers
                                        )
                                    }
                                } else {
                                    multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90f))
                                    multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(angle))
                                    minecraft.itemRenderer.renderItem(
                                        item,
                                        ModelTransformation.Mode.FIXED,
                                        15,
                                        15,
                                        this,
                                        vertexConsumers,
                                        0
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}