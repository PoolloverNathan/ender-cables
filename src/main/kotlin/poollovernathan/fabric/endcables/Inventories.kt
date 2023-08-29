package poollovernathan.fabric.endcables

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

interface ProxyInventory: Inventory, SidedInventory {
    val targetInventory: Inventory
    override fun clear() = targetInventory.clear()

    override fun size() = targetInventory.size()

    override fun isEmpty() = targetInventory.isEmpty()

    override fun getStack(slot: Int) = targetInventory.getStack(slot)

    override fun removeStack(slot: Int, amount: Int) = targetInventory.removeStack(slot, amount)

    override fun removeStack(slot: Int) = targetInventory.removeStack(slot)

    override fun setStack(slot: Int, stack: ItemStack?) = targetInventory.setStack(slot, stack)

    override fun markDirty() = targetInventory.markDirty()

    override fun canPlayerUse(player: PlayerEntity?) = targetInventory.canPlayerUse(player)
    override fun getAvailableSlots(side: Direction?) = targetInventory.let {
        if (it is SidedInventory) it.getAvailableSlots(side) else (0..<it.size()).toList().toIntArray()
    }

    override fun canInsert(slot: Int, stack: ItemStack?, dir: Direction?) =
        targetInventory.let { if (it is SidedInventory) it.canInsert(slot, stack, dir) else true }

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?) =
        targetInventory.let { if (it is SidedInventory) it.canExtract(slot, stack, dir) else true }
}
object DummyInventory: Inventory, SidedInventory {
    override fun clear() = Unit

    override fun size() = 0

    override fun isEmpty() = true

    override fun getStack(slot: Int) = ItemStack.EMPTY

    override fun removeStack(slot: Int, amount: Int) = ItemStack.EMPTY

    override fun removeStack(slot: Int) = ItemStack.EMPTY

    override fun setStack(slot: Int, stack: ItemStack?) = Unit

    override fun markDirty() = Unit

    override fun canPlayerUse(player: PlayerEntity?) = false
    override fun getAvailableSlots(side: Direction?) = intArrayOf()

    override fun canInsert(slot: Int, stack: ItemStack?, dir: Direction?) = false

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?) = false
}