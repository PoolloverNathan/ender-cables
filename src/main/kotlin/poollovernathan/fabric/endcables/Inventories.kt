package poollovernathan.fabric.endcables

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

interface ProxyInventory: Inventory, SidedInventory {
    val targetInventory: Inventory
    private val safeInventory; get() = targetInventory.takeUnless { it == this } ?: DummyInventory
    override fun clear() = stackOverflowFallback(DummyInventory.clear()) { safeInventory.clear() }

    override fun size() = stackOverflowFallback(DummyInventory.size()) { safeInventory.size() }

    override fun isEmpty() = stackOverflowFallback(DummyInventory.isEmpty) { safeInventory.isEmpty }

    override fun getStack(slot: Int): ItemStack =
        stackOverflowFallback(DummyInventory.getStack(slot)) { safeInventory.getStack(slot) }

    override fun removeStack(slot: Int, amount: Int): ItemStack =
        stackOverflowFallback(DummyInventory.removeStack(slot, amount)) { safeInventory.removeStack(slot, amount) }

    override fun removeStack(slot: Int): ItemStack =
        stackOverflowFallback(DummyInventory.removeStack(slot)) { safeInventory.removeStack(slot) }

    override fun setStack(slot: Int, stack: ItemStack?) =
        stackOverflowFallback(DummyInventory.setStack(slot, stack)) { safeInventory.setStack(slot, stack) }

    override fun markDirty() = stackOverflowFallback(DummyInventory.markDirty()) { safeInventory.markDirty() }

    override fun canPlayerUse(player: PlayerEntity?) =
        stackOverflowFallback(DummyInventory.canPlayerUse(player)) { safeInventory.canPlayerUse(player) }

    override fun getAvailableSlots(side: Direction?): IntArray =
        stackOverflowFallback(DummyInventory.getAvailableSlots(side)) {
            safeInventory.let {
                if (it is SidedInventory) it.getAvailableSlots(side) else (0..<it.size()).toList().toIntArray()
            }
        }

    override fun canInsert(slot: Int, stack: ItemStack?, dir: Direction?) = stackOverflowFallback(
        DummyInventory.canInsert(
            slot,
            stack,
            dir
        )
    ) { safeInventory.let { if (it is SidedInventory) it.canInsert(slot, stack, dir) else true } }

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?) = stackOverflowFallback(
        DummyInventory.canExtract(
            slot,
            stack,
            dir
        )
    ) { safeInventory.let { if (it is SidedInventory) it.canExtract(slot, stack, dir) else true } }
}

object DummyInventory: Inventory, SidedInventory {
    override fun clear() = Unit

    override fun size() = 0

    override fun isEmpty() = true

    override fun getStack(slot: Int): ItemStack = ItemStack.EMPTY

    override fun removeStack(slot: Int, amount: Int): ItemStack = ItemStack.EMPTY

    override fun removeStack(slot: Int): ItemStack = ItemStack.EMPTY

    override fun setStack(slot: Int, stack: ItemStack?) = Unit

    override fun markDirty() = Unit

    override fun canPlayerUse(player: PlayerEntity?) = false
    override fun getAvailableSlots(side: Direction?) = intArrayOf()

    override fun canInsert(slot: Int, stack: ItemStack?, dir: Direction?) = false

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?) = false
}