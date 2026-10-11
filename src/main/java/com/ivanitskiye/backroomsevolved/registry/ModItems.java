package com.ivanitskiye.backroomsevolved.registry;

import com.ivanitskiye.backroomsevolved.BackroomsEvolved;
import com.ivanitskiye.backroomsevolved.item.CameraItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BackroomsEvolved.MODID);

    public static final RegistryObject<Item> DAMP_CARPET = block(ModBlocks.DAMP_CARPET);
    public static final RegistryObject<Item> STAINED_CARPET = block(ModBlocks.STAINED_CARPET);
    public static final RegistryObject<Item> OLD_CARPET = block(ModBlocks.OLD_CARPET);
    public static final RegistryObject<Item> WALL_PANEL = block(ModBlocks.WALL_PANEL);
    public static final RegistryObject<Item> OLD_WALLPAPER = block(ModBlocks.OLD_WALLPAPER);
    public static final RegistryObject<Item> FLUORESCENT_LAMP = block(ModBlocks.FLUORESCENT_LAMP);
    public static final RegistryObject<Item> WHITE_LAMP = block(ModBlocks.WHITE_LAMP);
    public static final RegistryObject<Item> RED_LAMP = block(ModBlocks.RED_LAMP);
    public static final RegistryObject<Item> SANITY_ANCHOR = block(ModBlocks.SANITY_ANCHOR);

    public static final RegistryObject<Item> CAMERA = ITEMS.register("camera",
            () -> new CameraItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BATTERY = ITEMS.register("battery",
            () -> new Item(new Item.Properties()));

    private static RegistryObject<Item> block(RegistryObject<net.minecraft.world.level.block.Block> b) {
        return ITEMS.register(b.getId().getPath(), () -> new BlockItem(b.get(), new Item.Properties()));
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
