package com.ivanitskiye.backroomsevolved.registry;

import com.ivanitskiye.backroomsevolved.BackroomsEvolved;
import com.ivanitskiye.backroomsevolved.item.CameraChargeRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, BackroomsEvolved.MODID);

    public static final RegistryObject<SimpleCraftingRecipeSerializer<CameraChargeRecipe>> CAMERA_CHARGE =
            SERIALIZERS.register("camera_charge",
                    () -> new SimpleCraftingRecipeSerializer<>(CameraChargeRecipe::new));

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }
}
