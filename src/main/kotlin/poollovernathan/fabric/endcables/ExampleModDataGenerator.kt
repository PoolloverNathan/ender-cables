package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object ExampleModDataGenerator : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
		ExampleMod.registerables.forEach {
			it.registerDatagen(fabricDataGenerator)
		}
	}
}