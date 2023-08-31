package poollovernathan.fabric.endcables

import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3f

val minecraft
    get() = MinecraftClient.getInstance()

fun MatrixStack.translate(offset: Vec3f) = translate(offset.x.toDouble(), offset.y.toDouble(), offset.z.toDouble())
inline fun MatrixStack.push(block: (MatrixStack.Entry) -> Unit) {
    push()
    try {
        block(peek())
    } finally {
        pop()
    }
}

inline fun VertexConsumerProvider.render(layer: RenderLayer?, draw: (VertexConsumer) -> Unit) {
    val buffer = getBuffer(layer)
    draw(buffer)
    (this as VertexConsumerProvider.Immediate).drawCurrentLayer()
}