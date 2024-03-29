package com.yanny.ytech.configuration.item;

import com.yanny.ytech.configuration.MaterialItemType;
import com.yanny.ytech.configuration.SimpleItemType;
import com.yanny.ytech.configuration.Utils;
import com.yanny.ytech.registration.Holder;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import org.jetbrains.annotations.NotNull;

public class MaterialPickaxeItem extends PickaxeItem {
    public MaterialPickaxeItem(@NotNull Holder.ItemHolder holder) {
        super(holder.material.getTier(), 1, -2.8f, new Properties());
    }

    public static void registerRecipe(@NotNull Holder.ItemHolder holder, @NotNull RecipeOutput recipeConsumer) {
        switch (holder.material) {
            case ANTLER -> ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, holder.item.get())
                            .requires(SimpleItemType.ANTLER.itemTag)
                            .requires(SimpleItemType.SHARP_FLINT.itemTag)
                            .unlockedBy(Utils.getHasName(), RecipeProvider.has(SimpleItemType.SHARP_FLINT.itemTag))
                            .save(recipeConsumer, Utils.modLoc(holder.key));
            default -> ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, holder.item.get())
                    .define('S', Items.STICK)
                    .define('P', MaterialItemType.PLATE.itemTag.get(holder.material))
                    .define('R', MaterialItemType.ROD.itemTag.get(holder.material))
                    .define('#', MaterialItemType.INGOT.itemTag.get(holder.material))
                    .pattern("P#R")
                    .pattern(" S ")
                    .pattern(" S ")
                    .unlockedBy(Utils.getHasName(), RecipeProvider.has(MaterialItemType.INGOT.itemTag.get(holder.material)))
                    .save(recipeConsumer, Utils.modLoc(holder.key));
        }
    }
}
