package com.ivanitskiye.backroomsevolved.item;

import com.ivanitskiye.backroomsevolved.registry.ModItems;
import com.ivanitskiye.backroomsevolved.registry.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Бесформенный рецепт: камера + батарея = камера с зарядом +40.
 */
public class CameraChargeRecipe extends CustomRecipe {
    public CameraChargeRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        boolean camera = false;
        boolean battery = false;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            if (s.getItem() == ModItems.CAMERA.get()) {
                if (camera) return false;
                camera = true;
            } else if (s.getItem() == ModItems.BATTERY.get()) {
                if (battery) return false;
                battery = true;
            } else {
                return false;
            }
        }
        return camera && battery;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess access) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.getItem() == ModItems.CAMERA.get()) {
                ItemStack out = s.copy();
                CameraItem.setBattery(out, CameraItem.getBattery(out) + 40);
                out.getOrCreateTag().putBoolean(CameraItem.TAG_REC, false);
                return out;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CAMERA_CHARGE.get();
    }
}
