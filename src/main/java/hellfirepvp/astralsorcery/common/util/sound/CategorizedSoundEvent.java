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
// 1.21: SoundEvent's constructor is private (factory methods only), so this can no
// longer subclass it; it wraps the registered SoundEvent with its default category.
public class CategorizedSoundEvent {

    private final SoundEvent event;
    private final SoundSource category;

    public CategorizedSoundEvent(SoundEvent event, SoundSource category) {
        this.event = event;
        this.category = category;
    }

    public SoundEvent getSoundEvent() {
        return event;
    }

    public ResourceLocation getLocation() {
        return event.location();
    }

    public SoundSource getCategory() {
        return category;
    }

}
