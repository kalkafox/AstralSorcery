/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.quality;

import net.minecraft.network.chat.Component;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;

import java.util.Locale;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: GemQuality
 * Created by HellFirePvP
 * Date: 01.01.2021 / 14:12
 */
public enum GemQuality {

    BROKEN  (ChatFormatting.GRAY,  0.1F),
    FLAWED  (ChatFormatting.GRAY,  0.35F),
    MUNDANE (ChatFormatting.WHITE, 0.5F),
    CLEAR   (ChatFormatting.AQUA,  0.6F),
    FACETED (ChatFormatting.AQUA,  0.7F),
    GLEAMING(ChatFormatting.GOLD,  0.8F),
    FLAWLESS(ChatFormatting.GOLD,  1.0F);

    private final ChatFormatting color;
    private final float degree;

    GemQuality(ChatFormatting color, float degree) {
        this.color = color;
        this.degree = degree;
    }

    public float getDegree() {
        return degree;
    }

    public MutableComponent getDisplayName() {
        return Component.translatable("item.astralsorcery.gem_quality.%s", this.name().toLowerCase(Locale.ROOT))
                .withStyle(this.color);
    }
}
