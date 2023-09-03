package poollovernathan.fabric.endcables

import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

object ExampleMod : ModInitializer {
	val modId = "ender-cables"

    internal val logger = LoggerFactory.getLogger("ender-cables")

	internal val registerables: Array<Registerable> = arrayOf(
		CableBlock,
		CableEntity,
		ItemPacket,
		RiftBlock,
		RiftPearlItem,
		InserterBlock,
		InserterBlockEntity,
		ConsumerBlock,
		ConsumerBlockEntity,
	)

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Hello Fabric world!")
		registerables.forEach {
			it.register()
		}
	}

	fun id(path: String) = Identifier(modId, path)
	fun vid(path: String) = Identifier("minecraft", path)
}