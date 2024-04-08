package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant

@Suppress("UnstableApiUsage")
class TransactionalStorage<T: Any>(value: T): SnapshotParticipant<T>() {
    var value = value
        private set

    override fun createSnapshot() = value

    override fun readSnapshot(snapshot: T) {
        value = snapshot
    }

    operator fun set(transaction: TransactionContext, value: T) {
        updateSnapshots(transaction)
        this.value = value
    }
}