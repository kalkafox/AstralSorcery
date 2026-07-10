/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile.base;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileAreaOfInfluence
 * Created by HellFirePvP
 * Date: 25.04.2020 / 14:54
 */
// These methods are exclusively called on clientside
public interface TileAreaOfInfluence {

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public Color getEffectColor();

    @Nonnull
    public Vector3 getEffectPosition();

    public float getRadius();

    @Nonnull
    public BlockPos getEffectOriginPosition();

    @Nonnull
    public ResourceKey<Level> dimension();

    public boolean providesEffect();

}
