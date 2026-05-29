package net.hellomouse.createrailgun.ponder;

import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.hellomouse.createrailgun.CreateRailgun;
import net.hellomouse.createrailgun.registry.CRBlocks;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class CRPonderPlugin implements PonderPlugin {
    public static final ResourceLocation ELECTRIC = ResourceLocation.fromNamespaceAndPath(CreateRailgun.MODID, "railgun");

    @Override
    public @NotNull String getModId() {
        return CreateRailgun.MODID;
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.registerTag(ELECTRIC)
                .addToIndex()
                .item(CRBlocks.RAILGUN_CORE.get(), true, true)
                .item(CRBlocks.RAILGUN_RAIL.get(), true, false)
                .title("Electric Blocks")
                .description("Components which use electricity")
                .register();
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        var story1 = ResourceLocation.fromNamespaceAndPath(CreateRailgun.MODID, "railgun/railgun1");
        var story2 = ResourceLocation.fromNamespaceAndPath(CreateRailgun.MODID, "railgun/railgun2");

        helper.addStoryBoard(CRBlocks.RAILGUN_CORE.getId(), story1, CRPonderScenes.RAILGUN_PART_1, AllCreatePonderTags.CONTRAPTION_ASSEMBLY, ELECTRIC);
        helper.addStoryBoard(CRBlocks.RAILGUN_CORE.getId(), story2, CRPonderScenes.RAILGUN_PART_2, AllCreatePonderTags.CONTRAPTION_ASSEMBLY, ELECTRIC);
        helper.addStoryBoard(CRBlocks.RAILGUN_RAIL.getId(), story1, CRPonderScenes.RAILGUN_PART_1, AllCreatePonderTags.CONTRAPTION_ASSEMBLY, ELECTRIC);
        helper.addStoryBoard(CRBlocks.RAILGUN_RAIL.getId(), story2, CRPonderScenes.RAILGUN_PART_2, AllCreatePonderTags.CONTRAPTION_ASSEMBLY, ELECTRIC);
    }
}