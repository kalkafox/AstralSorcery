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

public class FadeSound extends SimpleSoundInstance implements TickableSoundInstance, SoundInstance {

    private Predicate<FadeSound> func = null;
    private boolean hasStoppedPlaying = false;
    private float volumeMultiplier = 1F;

    private float fade = 40;
    private float fadeOutTicks = 1;

    private int tick = 0;
    private int stopTick = 0;

    public FadeSound(CategorizedSoundEvent sound, float volume, float pitch, Vector3 pos, boolean isGlobal) {
        this(sound.getSoundEvent(), sound.getCategory(), volume, pitch, pos, isGlobal);
    }

    public FadeSound(SoundEvent sound, SoundSource category, float volume, float pitch, Vector3 pos, boolean isGlobal) {
        super(sound.getLocation(), category, volume, pitch, SoundInstance.createUnseededRandom(), true, 0, SoundInstance.Attenuation.LINEAR, pos.getX(), pos.getY(), pos.getZ(), isGlobal);
    }

    public void setRefreshFunction(Predicate<FadeSound> func) {
        this.func = func;
    }

    public <T extends FadeSound> T setFadeInTicks(float fade) {
        this.fade = fade;
        return (T) this;
    }

    public <T extends FadeSound> T setFadeOutTicks(float fadeOutTicks) {
        this.fadeOutTicks = fadeOutTicks;
        return (T) this;
    }

    @Override
    public boolean isStopped() {
        return (this.hasStoppedPlaying = (func == null || func.test(this))) && this.stopTick > this.fadeOutTicks;
    }

    public boolean hasStoppedPlaying() {
        return hasStoppedPlaying || !Minecraft.getInstance().getSoundManager().isActive(this);
    }

    public void setVolumeMultiplier(float volumeMultiplier) {
        this.volumeMultiplier = Mth.clamp(volumeMultiplier, 0F, 1F);
    }

    @Override
    public float getVolume() {
        float mulFadeIn = Mth.clamp(this.tick / this.fade, 0F, 1F);
        float mulFadeOut = Mth.clamp(1F - this.stopTick / this.fadeOutTicks, 0F, 1F);
        return mulFadeIn * mulFadeOut * super.getVolume()* volumeMultiplier;
    }

    @Override
    public void tick() {
        this.tick++;
        if (this.hasStoppedPlaying) {
            this.stopTick++;
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
