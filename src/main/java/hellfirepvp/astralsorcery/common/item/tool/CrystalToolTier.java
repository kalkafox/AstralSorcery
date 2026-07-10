/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.tool;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CrystalToolTier
 * Created by HellFirePvP
 * Date: 17.08.2019 / 16:13
 */
public class CrystalToolTier implements Tier {

    private static final CrystalToolTier INSTANCE = new CrystalToolTier();

    private CrystalToolTier() {}

    public static CrystalToolTier getInstance() {
        return INSTANCE;
    }

    @Override
    public int getUses() {
        return 16192;
    }

    @Override
    public float getSpeed() {
        return 4.5F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 3.5F;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
    }

    @Override
    public int getEnchantmentValue() {
        return 24;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }

    public int getMaxUses() {
        return this.getUses();
    }

    public float getAttackDamage() {
        return this.getAttackDamageBonus();
    }

    public int getLevel() {
        return 3;
    }
}
