package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper

object ExampleModDataGenerator : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
		ExampleMod.registerables.forEach {
			it.registerDatagen(fabricDataGenerator)
		}
	}
}