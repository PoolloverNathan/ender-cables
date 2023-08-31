package poollovernathan.fabric.endcables

import asa
import net.fabricmc.fabric.impl.client.rendering.BlockEntityRendererRegistryImpl
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.inventory.Inventory
import net.minecraft.util.math.Vec3f
import kotlin.math.min
import kotlin.math.sin

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
    ) {
        entity.apply {
            matrices.apply {
                pearl?.takeUnless { it.isEmpty }?.also {
                    push { entry ->
                        translate(0.5, 0.5, 0.5)
                        scale(-1.0f, -1.0f, -1.0f)
                        vertexConsumers.render(RenderLayer.getEndGateway()) { layer ->
                            outline.renderCuboid(entry, layer, 15, 15, 1f, 1f, 1f, 1f)
                        }
                    }
                    world?.getBlockEntity(target)?.takeIf { it is Inventory }.also {
                        push { entry ->
                            val angle = (world?.time ?: 0) + tickDelta
                            translate(0.5, 0.5, 0.5)
                            scale(0.5f, 0.5f, 0.5f)
                            if (it != null) {
                                multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(angle))
                                translate(-0.5, -0.5, -0.5)
                                minecraft.blockEntityRenderDispatcher.render(it, tickDelta, matrices, vertexConsumers)
                            } else {
                                multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90f))
                                multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(angle))
                                minecraft.itemRenderer.renderItem(it, ModelTransformation.Mode.FIXED, 15, 15, this, vertexConsumers, 0)
                            }
                        }
                    }
                }
            }
        }
    }
}