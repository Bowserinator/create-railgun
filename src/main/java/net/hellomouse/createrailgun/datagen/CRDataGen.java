package net.hellomouse.createrailgun.datagen;

import net.hellomouse.createrailgun.CreateRailgun;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = CreateRailgun.MODID)
public class CRDataGen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        generator.addProvider(event.includeClient(), new CRItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new CRBlockModelProvider(packOutput, existingFileHelper));
    }
}