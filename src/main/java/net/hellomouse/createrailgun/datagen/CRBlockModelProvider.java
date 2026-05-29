package net.hellomouse.createrailgun.datagen;

import net.hellomouse.createrailgun.CreateRailgun;
import net.hellomouse.createrailgun.block.RailgunRailBlock;
import net.hellomouse.createrailgun.registry.CRBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class CRBlockModelProvider extends BlockStateProvider {
    public CRBlockModelProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, CreateRailgun.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(CRBlocks.CORDIUM_CRYSTAL_BLOCK.get(), cubeAll(CRBlocks.CORDIUM_CRYSTAL_BLOCK.get()));

        ModelFile railModel = models().getExistingFile(modLoc("block/railgun_rail"));
        ModelFile railHotModel = models().getExistingFile(modLoc("block/railgun_rail_hot"));
        ModelFile railCoreModel = models().getExistingFile(modLoc("block/railgun_core"));

        getVariantBuilder(CRBlocks.RAILGUN_RAIL.get()).forAllStates(state -> {
            Direction facing = state.getValue(RailgunRailBlock.FACING);
            int brightness = state.getValue(RailgunRailBlock.BRIGHTNESS);
            int yRot = 0;
            int xRot = 0;

            switch (facing) {
                case WEST, EAST -> {yRot = 90;}
                case UP, DOWN -> {xRot = 90; yRot = 90;}
            }
            return ConfiguredModel.builder()
                    .modelFile(brightness > 0 ? railHotModel : railModel)
                    .rotationX(xRot).rotationY(yRot).build();
        });
        simpleBlockItem(CRBlocks.RAILGUN_RAIL.get(), railModel);

        directionalBlock(CRBlocks.RAILGUN_CORE.get(), railCoreModel);
        simpleBlockItem(CRBlocks.RAILGUN_CORE.get(), railCoreModel);
    }
}
