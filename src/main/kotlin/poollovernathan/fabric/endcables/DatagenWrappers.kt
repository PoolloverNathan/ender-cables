package poollovernathan.fabric.endcables

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.data.client.BlockStateModelGenerator
import net.minecraft.data.client.ItemModelGenerator
import net.minecraft.data.server.recipe.RecipeJsonProvider
import java.util.function.Consumer

fun FabricDataGenerator.recipes(generate: (Consumer<RecipeJsonProvider>) -> Unit) {
    addProvider(object: FabricRecipeProvider(this) {
        override fun generateRecipes(exporter: Consumer<RecipeJsonProvider>?) {
            generate(exporter ?: return)
        }
    })
}

fun FabricDataGenerator.language(generate: FabricLanguageProvider.TranslationBuilder.() -> Unit) {
    addProvider(object: FabricLanguageProvider(this) {
        override fun generateTranslations(translationBuilder: TranslationBuilder?) {
            generate(translationBuilder ?: return)
        }
    })
}

fun FabricDataGenerator.models(generate: ModelGenerationContext.() -> Unit) {
    addProvider {
        ModelGenerationContext(it).also { generate(it) }
    }
}

class ModelGenerationContext(generator: FabricDataGenerator): FabricModelProvider(generator) {
    private var block by SetOnce.default<BlockStateModelGenerator.() -> Unit>({}) {
        throw IllegalStateException("at most one blockstate block can be added")
    }
    private var item by SetOnce.default<ItemModelGenerator.() -> Unit>({}) {
        throw IllegalStateException("at most one item block can be added")
    }
    fun blockstate(generate: BlockStateModelGenerator.() -> Unit) {
        block = generate
    }
    fun item(generate: ItemModelGenerator.() -> Unit) {
        item = generate
    }

    override fun generateBlockStateModels(blockStateModelGenerator: BlockStateModelGenerator?) {
        if (blockStateModelGenerator != null) {
            block(blockStateModelGenerator)
        }
    }

    override fun generateItemModels(itemModelGenerator: ItemModelGenerator?) {
        if (itemModelGenerator != null) {
            item(itemModelGenerator)
        }
    }
}