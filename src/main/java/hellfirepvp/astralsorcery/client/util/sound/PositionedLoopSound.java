/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util.sound;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.sound.CategorizedSoundEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;

import java.util.function.Predicate;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PositionedLoopSound
 * Created by HellFirePvP
 * Date: 30.06.2019 / 22:59
 */
public class PositionedLoopSound extends SimpleSoundInstance implements TickableSoundInstance, SoundInstance {

    private Predicate<PositionedLoopSound> func = null;
    private boolean hasStoppedPlaying = false;
    private float volumeMultiplier = 1F;

    public PositionedLoopSound(CategorizedSoundEvent sound, float volume, float pitch, Vector3 pos, boolean isGlobal) {
        this(sound.getSoundEvent(), sound.getCategory(), volume, pitch, pos, isGlobal);
    }

    public PositionedLoopSound(SoundEvent sound, SoundSource category, float volume, float pitch, Vector3 pos, boolean isGlobal) {
        super(sound.getLocation(), category, volume, pitch, SoundInstance.createUnseededRandom(), true, 0, SoundInstance.Attenuation.LINEAR, pos.getX(), pos.getY(), pos.getZ(), isGlobal);
    }

    public void setRefreshFunction(Predicate<PositionedLoopSound> func) {
        this.func = func;
    }

    @Override
    public boolean isStopped() {
        hasStoppedPlaying = func == null || func.test(this);
        return hasStoppedPlaying;
    }

    public boolean hasStoppedPlaying() {
        return hasStoppedPlaying || !Minecraft.getInstance().getSoundManager().isActive(this);
    }

    public void setVolumeMultiplier(float volumeMultiplier) {
        this.volumeMultiplier = Mth.clamp(volumeMultiplier, 0F, 1F);
    }

    @Override
    public float getVolume() {
        return super.getVolume() * volumeMultiplier;
    }

    @Override
    public void tick() {}

}