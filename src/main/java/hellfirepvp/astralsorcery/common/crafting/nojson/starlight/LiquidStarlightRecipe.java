/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.nojson.starlight;

import hellfirepvp.astralsorcery.common.crafting.nojson.CustomRecipe;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LiquidStarlightRecipe
 * Created by HellFirePvP
 * Date: 30.09.2019 / 20:27
 */
public abstract class LiquidStarlightRecipe extends CustomRecipe {

    protected static final Random random = new Random();
    private static final int WORLD_TIME_TOLERANCE = 10;

    public LiquidStarlightRecipe(ResourceLocation key) {
        super(key);
    }

    @OnlyIn(Dist.CLIENT)
    public abstract List<Ingredient> getInputForRender();

    @OnlyIn(Dist.CLIENT)
    public abstract List<Ingredient> getOutputForRender();

    public abstract boolean doesStartRecipe(ItemStack item);

    public abstract boolean matches(ItemEntity trigger, Level level, BlockPos at);

    public abstract void doServerCraftTick(ItemEntity trigger, Level level, BlockPos at);

    @OnlyIn(Dist.CLIENT)
    public abstract void doClientEffectTick(ItemEntity trigger, Level level, BlockPos at);

    protected final List<Entity> getEntitiesInBlock(LevelAccessor level, BlockPos pos) {
        return level.getEntitiesOfClass(Entity.class, new AABB(pos));
    }

    @Nullable
    protected final ItemStack consumeItemEntityInBlock(LevelAccessor level, BlockPos pos, Item itemClass) {
        return consumeItemEntityInBlock(level, pos, 1, stack ->
                itemClass.getClass().isAssignableFrom(stack.getItem().getClass()));
    }

    @Nullable
    protected final ItemStack consumeItemEntityInBlock(LevelAccessor level, BlockPos pos, int count, Predicate<ItemStack> match) {
        List<Entity> entities = getEntitiesInBlock(level, pos).stream()
                .filter(e -> e instanceof ItemEntity)
                .collect(Collectors.toList());
        for (Entity e : entities) {
            ItemEntity ie = (ItemEntity) e;
            if (ie.isAlive() &&
                    !ie.getItem().isEmpty() &&
                    ie.getItem().getCount() >= count &&
                    match.test(ie.getItem())) {

                ItemStack stored = ie.getItem();
                ItemStack found = ItemUtils.copyStackWithSize(stored, count);

                stored.shrink(count);
                ie.setItem(stored);
                return found;
            }
        }
        return null;
    }

    protected final int getAndIncrementCraftingTick(Entity e) {
        int tick = getCraftingTick(e);
        setCraftingTick(e, tick + 1);
        return tick;
    }

    protected final void setCraftingTick(Entity e, int tick) {
        long wTick = e.getCommandSenderWorld().getGameTime();

        CompoundTag nbt = NBTHelper.getPersistentData(e);
        nbt.putInt("craftTick", tick);
        nbt.putLong("wCraftTick", wTick);
    }

    protected final int getCraftingTick(Entity e) {
        long wTick = e.getCommandSenderWorld().getGameTime();

        CompoundTag nbt = NBTHelper.getPersistentData(e);
        if (!nbt.contains("wCraftTick", Constants.NBT.TAG_LONG)) {
            return 0;
        }

        long savedWTick = nbt.getLong("wCraftTick");
        if (Math.abs(wTick - savedWTick) > WORLD_TIME_TOLERANCE) {
            return 0;
        } else {
            return nbt.getInt("craftTick");
        }
    }

}
