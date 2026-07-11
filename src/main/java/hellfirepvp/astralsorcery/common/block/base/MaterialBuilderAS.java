/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.base;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MaterialBuilderAS
 * Created by HellFirePvP
 * Date: 30.05.2019 / 23:00
 *
 * NOTE: as of 1.21 vanilla no longer has a standalone `Material` type (it was folded into
 * BlockBehaviour.Properties / MapColor). This builder is kept as a thin MapColor holder for the
 * few flags callers still configure; it no longer produces a distinct "material" object.
 */
public class MaterialBuilderAS {

    private final MapColor color;
    private PushReaction pushReaction = PushReaction.NORMAL;
    private boolean blocksMovement = true;
    private boolean flammable = false;
    private boolean liquid = false;
    private boolean isReplaceable = false;
    private boolean isSolid = true;
    private boolean isOpaque = true;

    public MaterialBuilderAS(MapColor color) {
        this.color = color;
    }

    public MaterialBuilderAS liquid() {
        this.liquid = true;
        return this;
    }

    public MaterialBuilderAS notSolid() {
        this.isSolid = false;
        return this;
    }

    public MaterialBuilderAS doesNotBlockMovement() {
        this.blocksMovement = false;
        return this;
    }

    public MaterialBuilderAS notSolidBlocking() {
        this.isOpaque = false;
        return this;
    }

    public MaterialBuilderAS flammable() {
        this.flammable = true;
        return this;
    }

    public MaterialBuilderAS replaceable() {
        this.isReplaceable = true;
        return this;
    }

    public MaterialBuilderAS destroyOnPush() {
        this.pushReaction = PushReaction.DESTROY;
        return this;
    }

    public MaterialBuilderAS notPushable() {
        this.pushReaction = PushReaction.BLOCK;
        return this;
    }

    public MapColor build() {
        return this.color;
    }
}
