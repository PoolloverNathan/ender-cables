package poollovernathan.fabric.endcables

import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexConsumerProvider.Immediate
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.state.property.Properties
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*
import net.minecraft.util.math.Direction.Axis.*
import net.minecraft.util.math.Direction.AxisDirection.*
import net.minecraft.util.math.Vec3f
import net.minecraft.util.shape.VoxelShapes
import java.nio.Buffer
import kotlin.math.sign

class CableRenderer private constructor(private val ctx: BlockEntityRendererFactory.Context?) : BlockEntityRenderer<CableEntity> {
    companion object: ClientRegisterable, HasID by CableEntity, BlockEntityRendererFactory<CableEntity> {
        override fun registerClient() {
            BlockEntityRendererFactories.register(CableEntity.type, CableRenderer)
        }

        override fun create(ctx: BlockEntityRendererFactory.Context?): BlockEntityRenderer<CableEntity> = CableRenderer(ctx)

        val packetPart = ModelPart(listOf(ModelPart.Cuboid(0, 0, 8.0f, 8.0f, 8.0f, 0f, 0f, 0f, 2f, 2f, 2f, false, 1f, 1f)), mapOf())
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
        entity?.packet?.also {
            val color = it.getColor()
            matrices?.push {
                val solid = vertexConsumers?.getBuffer(RenderLayer.getSolid())
                var progress = when (((entity.world?.time ?: 0) - entity.insertionTime).sign) {
                    0 -> tickDelta
                    1 -> 1f
                    -1 /* time-travel? */ -> 0f
                    else -> 0f
                }
                progress -= 0.5f
                if (!entity.forward) progress *= -1f
                progress *= 16f
                matrices.translate(0.5, 0.5, 0.5)
                matrices.translate(entity.cachedState[Properties.AXIS].towards(POSITIVE).unitVector * progress)
                packetPart.render(matrices, solid, 15, 0, color.rF, color.gF, color.bF, color.aF)
                (vertexConsumers as Immediate).drawCurrentLayer()
            }
        }
        entity?.also {
            val portal = vertexConsumers?.getBuffer(RenderLayer.getEndGateway())
            innerPart(it.cachedState[Properties.AXIS]).render(matrices, portal, 15, 0)
            (vertexConsumers as Immediate).drawCurrentLayer()
        }
    }
}

fun MatrixStack.translate(offset: Vec3f) = translate(offset.x.toDouble(), offset.y.toDouble(),
    offset.z.toDouble()
)

operator fun Vec3f.times(scale: Float) = Vec3f(x * scale, y * scale, z * scale)

fun MatrixStack.push(block: (MatrixStack.Entry) -> Unit) {
    push()
    try {
        block(peek())
    } finally {
        pop()
    }
}
