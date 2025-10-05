package fuzs.enchantinginfuser.data;

import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.puzzleslib.api.data.v2.AbstractRecipeProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

public class ModRecipeProvider extends AbstractRecipeProvider {

    public ModRecipeProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addRecipes(RecipeOutput recipeOutput) {
        this.shaped(RecipeCategory.DECORATIONS, ModRegistry.INFUSER_ITEM.value())
                .define('B', Items.BOOK)
                .define('#', Items.CRYING_OBSIDIAN)
                .define('A', Items.AMETHYST_SHARD)
                .define('T', Items.ENCHANTING_TABLE)
                .pattern(" B ")
                .pattern("A#A")
                .pattern("#T#")
                .unlockedBy(getHasName(Items.AMETHYST_SHARD), this.has(Items.AMETHYST_SHARD))
                .save(recipeOutput);
        this.shaped(RecipeCategory.DECORATIONS, ModRegistry.ADVANCED_INFUSER_ITEM.value())
                .define('B', Items.BOOK)
                .define('#', Items.CRYING_OBSIDIAN)
                .define('A', Items.NETHERITE_INGOT)
                .define('T', ModRegistry.INFUSER_ITEM.value())
                .pattern(" B ")
                .pattern("A#A")
                .pattern("#T#")
                .unlockedBy(getHasName(Items.NETHERITE_INGOT), this.has(Items.NETHERITE_INGOT))
                .save(recipeOutput);
    }
}
