package poollovernathan.fabric.endcables

import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

abstract class BlockWithEntity<T: BlockEntity>(settings: Settings, val entityType: BlockEntityType<T>): Block(settings),
    BlockEntityProvider {
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? = Unit.takeIf { state.block == this }?.let { entityType.instantiate(pos, state) }
}

abstract class BlockWithTickingEntity<T: BlockEntity>(
    settings: Settings,
    entityType: BlockEntityType<T>
): BlockWithEntity<T>(settings, entityType), BlockEntityTicker<T> {
    override fun <U: BlockEntity?> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<U>
    ): BlockEntityTicker<U>? = (this as? BlockEntityTicker<T>)?.coerce()
}