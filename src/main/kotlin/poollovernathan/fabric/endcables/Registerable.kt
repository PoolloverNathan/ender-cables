package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

interface Registerable {
    fun register()
    fun registerDatagen(generator: FabricDataGenerator) {}


}