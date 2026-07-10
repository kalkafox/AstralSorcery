/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.lens;

import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.item.base.client.ItemDynamicColor;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.tile.TileLens;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemColoredLens
 * Created by HellFirePvP
 * Date: 24.08.2019 / 21:23
 */
public abstract class ItemColoredLens extends Item implements ItemDynamicColor {

    private final LensColorType lensColorType;

    protected ItemColoredLens(LensColorType colorType) {
        this(colorType, new Properties().group(CommonProxy.ITEM_GROUP_AS));
    }

    protected ItemColoredLens(LensColorType colorType, Properties properties) {
        super(properties);
        this.lensColorType = colorType;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        Level level = ctx.getLevel();
        if (!level.isClientSide() && player != null) {
            TileLens lens = MiscUtils.getTileAt(level, ctx.getBlockPos(), TileLens.class, false);
            if (lens != null) {
                ItemStack held = ctx.getItem();
                LensColorType oldType = lens.setColorType(this.lensColorType);

                if (!player.isCreative()) {
                    held.setCount(held.getCount() - 1);
                    if (held.getCount() <= 0) {
                        player.setHeldItem(ctx.getHand(), ItemStack.EMPTY);
                    }
                }

                SoundHelper.playSoundAround(SoundsAS.BLOCK_COLOREDLENS_ATTACH, level, ctx.getBlockPos(), 0.8F, 1.5F);
                if (oldType != null) {
                    player.inventory.hurtArmor(level, oldType.getStack());
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getColor(ItemStack stack, int tintIndex) {
        return this.lensColorType.getColor().getRGB();
    }
}
