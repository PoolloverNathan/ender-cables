package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import java.util.*

class PacketsContainer(val packets: Array<TransactionalStorage<Optional<CableTransferPacket>>>) {
    constructor(size: UInt): this(Array(size asan Int) { TransactionalStorage(Optional.empty()) })
    fun writeNbt(nbt: NbtCompound, key: String) {
        NbtList().also { list ->
            packets.forEach {
                if (it.value.isPresent) {
                    list.add(NbtCompound().also(it.value.get()::toNbt))
                }
            }
            nbt.put(key, list)
        }
    }
    fun readNbt(nbt: NbtCompound, key: String, transaction: Transaction) {
        transaction { t ->
            nbt.getList(key, NbtList.COMPOUND_TYPE asan Int).also {
                it.forEachIndexed { index, packet ->
                    packets[index][t] = Optional.ofNullable(
                        CableTransferPacket.fromNbt(
                            packet as? NbtCompound ?: return@forEachIndexed
                        )
                    )
                }
            }
            t.commit()
        }
    }
}