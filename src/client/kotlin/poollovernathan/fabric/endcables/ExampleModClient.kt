package poollovernathan.fabric.endcables

import net.fabricmc.api.ClientModInitializer

object ExampleModClient : ClientModInitializer {
	internal var clientRegisterables = listOf<ClientRegisterable>(
		CableRenderer,
		RiftPearlItemColorProvider
	)

	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		clientRegisterables.forEach {
			it.registerClient()
		}
	}
}