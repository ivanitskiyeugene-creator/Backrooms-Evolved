package com.ivanitskiye.backroomsevolved;

import com.ivanitskiye.backroomsevolved.network.ModNetwork;
import com.ivanitskiye.backroomsevolved.registry.ModBlocks;
import com.ivanitskiye.backroomsevolved.registry.ModItems;
import com.ivanitskiye.backroomsevolved.registry.ModRecipeSerializers;
import com.ivanitskiye.backroomsevolved.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(BackroomsEvolved.MODID)
public class BackroomsEvolved {
    public static final String MODID = "backroomsevolved";

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(ForgeRegistries.CREATIVE_MODE_TABS, MODID);

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MODID))
                    .icon(() -> ModItems.CAMERA.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        ModBlocks.BLOCKS.getEntries().forEach(ro -> output.accept(ro.get()));
                        output.accept(ModItems.CAMERA.get());
                        output.accept(ModItems.BATTERY.get());
                    })
                    .build());

    public BackroomsEvolved() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModSounds.register(modBus);
        ModRecipeSerializers.register(modBus);
        TABS.register(modBus);
        ModNetwork.register();

        modBus.register(this);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.ivanitskiye.backroomsevolved.client.ClientSetup.init(modBus);
        }
    }

    @SubscribeEvent
    public void onCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        // Дублируем контент во ванильные вкладки для удобства строителей
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            ModBlocks.BLOCKS.getEntries().forEach(ro -> event.accept(ro.get()));
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.CAMERA.get());
            event.accept(ModItems.BATTERY.get());
        }
    }

    public static ResourceLocation loc(String path) {
        return new ResourceLocation(MODID, path);
    }
}
