package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.minecraft.block.Block
import net.minecraft.client.color.item.ItemColorProvider
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

object RiftPearlItemColorProvider: ItemColorProvider, ClientRegisterable {
    override fun getColor(stack: ItemStack?, tintIndex: Int): Int {
        return if (RiftPearlItem.getTarget(stack ?: ItemStack.EMPTY) == null) {
            Color(1.0, 0.0, 0.0)
        } else {
            Color(1.0, 0.5, 1.0)
        }.rgb
    }

    override fun registerClient() {
        RiftPearlItem.colorProvider = RiftPearlItemColorProvider
    }
}

var Item.colorProvider
    get() = ColorProviderRegistry.ITEM[this]
    set(value) = ColorProviderRegistry.ITEM.register(value, this)
var Block.colorProvider
    get() = ColorProviderRegistry.BLOCK[this]
    set(value) = ColorProviderRegistry.BLOCK.register(value, this)