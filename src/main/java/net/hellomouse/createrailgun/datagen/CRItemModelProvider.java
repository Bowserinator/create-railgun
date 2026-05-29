package net.hellomouse.createrailgun.datagen;

import net.hellomouse.createrailgun.CreateRailgun;
import net.hellomouse.createrailgun.registry.CRItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class CRItemModelProvider extends ItemModelProvider {
    public CRItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CreateRailgun.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(CRItems.CORDIUM_CRYSTAL.get());
        basicItem(CRItems.FILTERED_MAGMA_SLAG.get());
        basicItem(CRItems.MAGMA_SLAG.get());
        basicItem(CRItems.NETHERITE_FILTER.get());
        basicItem(CRItems.RAILGUN_SLUG.get());

        // 2. BlockItem (Tells the item to use the block's 3D model)
        // This assumes your block model is at assets/yourmodid/models/block/railgun_core.json
        // withExistingParent(ModBlocks.RAILGUN_CORE.getId().getPath(), modLoc("block/railgun_core"));
    }
}