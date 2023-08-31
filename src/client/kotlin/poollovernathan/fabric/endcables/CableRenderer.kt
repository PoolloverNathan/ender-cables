package poollovernathan.fabric.endcables

import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.state.property.Properties
import net.minecraft.util.math.Direction.*
import net.minecraft.util.math.Direction.Axis.*
import net.minecraft.util.math.Direction.AxisDirection.*
import kotlin.math.sign

class CableRenderer private constructor(private val ctx: BlockEntityRendererFactory.Context?) : BlockEntityRenderer<CableEntity> {
    companion object: ClientRegisterable, HasID by CableEntity, BlockEntityRendererFactory<CableEntity> {
        override fun registerClient() {
            BlockEntityRendererFactories.register(CableEntity.type, CableRenderer)
        }

        override fun create(ctx: BlockEntityRendererFactory.Context?): BlockEntityRenderer<CableEntity> = CableRenderer(ctx)

        val packetPart; get(/* for debugging */) = ModelPart(listOf(ModelPart.Cuboid(10, 10, 8.0f, 8.0f, 8.0f, 0f, 0f, 0f, 2f, 2f, 2f, false, 1f, 1f)), mapOf())
        fun innerPart(axis: Axis) =
            (3.0f to 7.999f).run {
                when (axis) {
                    X -> second to first too first
                    Y -> first to second too first
                    Z -> first to first too second
                }
            }.run {
                ModelPart(listOf(ModelPart.Cuboid(0, 0, 8.0f, 8.0f, 8.0f, 0f, 0f, 0f, -first, -second, -third, false, 1f, 1f)), mapOf())
            }
    }
    override fun render(
        entity: CableEntity?,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider?,
        light: Int,
        overlay: Int
    ) {
        entity?.apply {
            packet?.apply {
                var color = getColor()
                color = Color.white()
                matrices?.push {
                    vertexConsumers?.render(RenderLayer.getSolid()) {
                        var progress = when (((world?.time ?: 0) - insertionTime).sign) {
                            0                     -> tickDelta
                            1                     -> 1f
                            -1 /* time-travel? */ -> 0f
                            else                  -> 0f
                        }
                        progress = ((world!!.time % 4) + tickDelta) / 4 % 1
                        progress -= 0.5f
                        if (!forward) progress *= -1f
                        matrices.translate(cachedState[Properties.AXIS].towards(POSITIVE).unitVector * progress)
                        packetPart.render(matrices, it, 15, 15, color.rF, color.gF, color.bF, color.aF)
                    }
                }
            }
            vertexConsumers?.render(RenderLayer.getEndPortal()) {
                innerPart(cachedState[Properties.AXIS]).render(matrices, it, 15, 0)
            }
        }
    }
}
