package poollovernathan.fabric.endcables

import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

interface ICableTransferPacket {
    fun readNbt(nbt: NbtCompound)
    fun writeNbt(nbt: NbtCompound)
    fun getTexture(): Identifier
}