/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CategorizedSoundEvent
 * Created by HellFirePvP
 * Date: 30.06.2019 / 22:58
 */
public class CategorizedSoundEvent extends SoundEvent {

    private final SoundSource category;

    public CategorizedSoundEvent(ResourceLocation soundNameIn, SoundSource category) {
        super(soundNameIn);
        this.category = category;
    }

    public SoundSource getCategory() {
        return category;
    }

}