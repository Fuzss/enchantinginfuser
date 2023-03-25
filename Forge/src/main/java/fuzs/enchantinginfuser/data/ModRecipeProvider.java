package fuzs.enchantinginfuser.data;

import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.puzzleslib.api.data.v1.AbstractRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class ModRecipeProvider extends AbstractRecipeProvider {

    public ModRecipeProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> exporter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModRegistry.INFUSER_ITEM.get())
                .define('B', Items.BOOK)
                .define('#', Items.CRYING_OBSIDIAN)
                .define('A', Items.AMETHYST_SHARD)
                .define('T', Items.ENCHANTING_TABLE)
                .pattern(" B ")
                .pattern("A#A")
                .pattern("#T#")
                .unlockedBy(getHasName(Items.AMETHYST_SHARD), has(Items.AMETHYST_SHARD))
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModRegistry.ADVANCED_INFUSER_ITEM.get())
                .define('B', Items.BOOK)
                .define('#', Items.CRYING_OBSIDIAN)
                .define('A', Items.NETHERITE_INGOT)
                .define('T', ModRegistry.INFUSER_ITEM.get())
                .pattern(" B ")
                .pattern("A#A")
                .pattern("#T#")
                .unlockedBy(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                .save(exporter);
    }
}
