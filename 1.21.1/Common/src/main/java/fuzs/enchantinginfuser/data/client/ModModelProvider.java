package fuzs.enchantinginfuser.data.client;

import fuzs.enchantinginfuser.init.ModRegistry;
import fuzs.puzzleslib.api.client.data.v2.AbstractModelProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ModModelProvider extends AbstractModelProvider {
    public static final ModelTemplate ENCHANTING_TABLE_MODEL_TEMPLATE = createBlockModelTemplate(
            getLocation(Blocks.ENCHANTING_TABLE), "", TextureSlot.PARTICLE, TextureSlot.TOP, TextureSlot.BOTTOM,
            TextureSlot.SIDE
    );
    public static final TexturedModel.Provider ENCHANTING_TABLE_TEXTURED_MODEL = TexturedModel.createDefault(
            (Block block) -> {
                return TextureMapping.cubeBottomTop(block).put(TextureSlot.PARTICLE,
                        TextureMapping.getBlockTexture(block, "_bottom")
                );
            }, ENCHANTING_TABLE_MODEL_TEMPLATE);

    public ModModelProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addBlockModels(BlockModelGenerators builder) {
        createEnchantingInfuserBlock(builder, ModRegistry.INFUSER_BLOCK.value());
        createEnchantingInfuserBlock(builder, ModRegistry.ADVANCED_INFUSER_BLOCK.value());
    }

    static void createEnchantingInfuserBlock(BlockModelGenerators builder, Block block) {
        builder.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block,
                ENCHANTING_TABLE_TEXTURED_MODEL.create(block, builder.modelOutput)
        ));
    }
}
