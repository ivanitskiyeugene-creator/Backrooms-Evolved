package com.ivanitskiye.backroomsevolved.registry;

import com.ivanitskiye.backroomsevolved.BackroomsEvolved;
import com.ivanitskiye.backroomsevolved.block.AlarmLampBlock;
import com.ivanitskiye.backroomsevolved.block.BackroomsLampBlock;
import com.ivanitskiye.backroomsevolved.block.DampCarpetBlock;
import com.ivanitskiye.backroomsevolved.block.FlickerLampBlock;
import com.ivanitskiye.backroomsevolved.block.FootprintCarpetBlock;
import com.ivanitskiye.backroomsevolved.block.SanityAnchorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BackroomsEvolved.MODID);

    private static BlockBehaviour.Properties carpet() {
        return BlockBehaviour.Properties.copy(Blocks.YELLOW_WOOL);
    }

    private static BlockBehaviour.Properties lampProps() {
        return BlockBehaviour.Properties.copy(Blocks.GLASS)
                .lightLevel(s -> BackroomsLampBlock.lightOf(s) ? 15 : 0);
    }

    public static final RegistryObject<Block> DAMP_CARPET = BLOCKS.register("damp_carpet",
            () -> new DampCarpetBlock(carpet()));

    public static final RegistryObject<Block> STAINED_CARPET = BLOCKS.register("stained_carpet",
            () -> new Block(carpet()));

    public static final RegistryObject<Block> OLD_CARPET = BLOCKS.register("old_carpet",
            () -> new FootprintCarpetBlock(carpet()));

    public static final RegistryObject<Block> WALL_PANEL = BLOCKS.register("wall_panel",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SMOOTH_QUARTZ)));

    public static final RegistryObject<Block> OLD_WALLPAPER = BLOCKS.register("old_wallpaper",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));

    public static final RegistryObject<Block> FLUORESCENT_LAMP = BLOCKS.register("fluorescent_lamp",
            () -> new FlickerLampBlock(lampProps()));

    public static final RegistryObject<Block> WHITE_LAMP = BLOCKS.register("white_lamp",
            () -> new FlickerLampBlock(lampProps()));

    public static final RegistryObject<Block> RED_LAMP = BLOCKS.register("red_lamp",
            () -> new AlarmLampBlock(lampProps()));

    public static final RegistryObject<Block> SANITY_ANCHOR = BLOCKS.register("sanity_anchor",
            () -> new SanityAnchorBlock(BlockBehaviour.Properties.copy(Blocks.LANTERN)
                    .lightLevel(s -> 14)));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

    /** Является ли блок "бекрумс-блоком" для расчёта плотности/санити/эха. */
    public static boolean isBackroomsBlock(Block block) {
        return block instanceof FootprintCarpetBlock
                || block instanceof BackroomsLampBlock
                || block == WALL_PANEL.get()
                || block == OLD_WALLPAPER.get()
                || block == SANITY_ANCHOR.get();
    }
}
