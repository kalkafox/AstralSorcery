/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.sound;

import hellfirepvp.astralsorcery.client.util.sound.FadeLoopSound;
import hellfirepvp.astralsorcery.client.util.sound.FadeSound;
import hellfirepvp.astralsorcery.client.util.sound.PositionedLoopSound;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Predicate;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SoundHelper
 * Created by HellFirePvP
 * Date: 30.06.2019 / 22:57
 */
public class SoundHelper {

    public static void playSoundAround(CategorizedSoundEvent sound, Level level, Vec3i position, float volume, float pitch) {
        playSoundAround(sound.getSoundEvent(), sound.getCategory(), level, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    public static void playSoundAround(CategorizedSoundEvent sound, Level level, Vector3 position, float volume, float pitch) {
        playSoundAround(sound.getSoundEvent(), sound.getCategory(), level, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    // The sound's own category wins over the passed one, mirroring the pre-1.21
    // behavior where a CategorizedSoundEvent overrode the explicit argument.
    public static void playSoundAround(CategorizedSoundEvent sound, SoundSource category, Level level, Vec3i position, float volume, float pitch) {
        playSoundAround(sound.getSoundEvent(), sound.getCategory(), level, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    public static void playSoundAround(CategorizedSoundEvent sound, SoundSource category, Level level, Vector3 position, float volume, float pitch) {
        playSoundAround(sound.getSoundEvent(), sound.getCategory(), level, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    public static void playSoundAround(SoundEvent sound, Level level, Vec3i position, float volume, float pitch) {
        playSoundAround(sound, SoundSource.MASTER, level, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    public static void playSoundAround(SoundEvent sound, SoundSource category, Level level, Vec3i position, float volume, float pitch) {
        playSoundAround(sound, category, level, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    public static void playSoundAround(SoundEvent sound, Level level, Vector3 position, float volume, float pitch) {
        playSoundAround(sound, SoundSource.MASTER, level, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    public static void playSoundAround(SoundEvent sound, SoundSource category, Level level, Vector3 position, float volume, float pitch) {
        playSoundAround(sound, category, level, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    public static void playSoundAround(SoundEvent sound, SoundSource category, Level level, double posX, double posY, double posZ, float volume, float pitch) {
        level.playSound(null, posX, posY, posZ, sound, category, volume, pitch);
    }

    @OnlyIn(Dist.CLIENT)
    public static PositionedLoopSound playSoundLoopClient(CategorizedSoundEvent sound, Vector3 pos, float volume, float pitch, boolean isGlobal, Predicate<PositionedLoopSound> func) {
        return playSoundLoopClient(sound.getSoundEvent(), sound.getCategory(), pos, volume, pitch, isGlobal, func);
    }

    @OnlyIn(Dist.CLIENT)
    public static PositionedLoopSound playSoundLoopClient(SoundEvent sound, SoundSource cat, Vector3 pos, float volume, float pitch, boolean isGlobal, Predicate<PositionedLoopSound> func) {
        PositionedLoopSound posSound = new PositionedLoopSound(sound, cat, volume, pitch, pos, isGlobal);
        posSound.setRefreshFunction(func);
        Minecraft.getInstance().getSoundManager().play(posSound);
        return posSound;
    }

    @OnlyIn(Dist.CLIENT)
    public static FadeLoopSound playSoundLoopFadeInClient(CategorizedSoundEvent sound, Vector3 pos, float volume, float pitch, boolean isGlobal, Predicate<PositionedLoopSound> func) {
        return playSoundLoopFadeInClient(sound.getSoundEvent(), sound.getCategory(), pos, volume, pitch, isGlobal, func);
    }

    @OnlyIn(Dist.CLIENT)
    public static FadeLoopSound playSoundLoopFadeInClient(SoundEvent sound, SoundSource cat, Vector3 pos, float volume, float pitch, boolean isGlobal, Predicate<PositionedLoopSound> func) {
        FadeLoopSound posSound = new FadeLoopSound(sound, cat, volume, pitch, pos, isGlobal);
        posSound.setRefreshFunction(func);
        Minecraft.getInstance().getSoundManager().play(posSound);
        return posSound;
    }

    @OnlyIn(Dist.CLIENT)
    public static FadeSound playSoundFadeInClient(CategorizedSoundEvent sound, Vector3 pos, float volume, float pitch, boolean isGlobal, Predicate<FadeSound> func) {
        return playSoundFadeInClient(sound.getSoundEvent(), sound.getCategory(), pos, volume, pitch, isGlobal, func);
    }

    @OnlyIn(Dist.CLIENT)
    public static FadeSound playSoundFadeInClient(SoundEvent sound, SoundSource cat, Vector3 pos, float volume, float pitch, boolean isGlobal, Predicate<FadeSound> func) {
        FadeSound posSound = new FadeSound(sound, cat, volume, pitch, pos, isGlobal);
        posSound.setRefreshFunction(func);
        Minecraft.getInstance().getSoundManager().play(posSound);
        return posSound;
    }

    @OnlyIn(Dist.CLIENT)
    public static float getSoundVolume(SoundSource cat) {
        return Minecraft.getInstance().options.getSoundSourceVolume(cat);
    }

    @OnlyIn(Dist.CLIENT)
    public static void playSoundClient(CategorizedSoundEvent sound, float volume, float pitch) {
        playSoundClient(sound.getSoundEvent(), volume, pitch);
    }

    @OnlyIn(Dist.CLIENT)
    public static void playSoundClient(SoundEvent sound, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    @OnlyIn(Dist.CLIENT)
    public static void playSoundClientWorld(CategorizedSoundEvent sound, BlockPos pos, float volume, float pitch) {
        playSoundClientWorld(sound.getSoundEvent(), sound.getCategory(), pos, volume, pitch);
    }

    @OnlyIn(Dist.CLIENT)
    public static void playSoundClientWorld(SoundEvent sound, SoundSource cat, BlockPos pos, float volume, float pitch) {
        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().level.playSound(Minecraft.getInstance().player, pos.getX(), pos.getY(), pos.getZ(), sound, cat, volume, pitch);
        }
    }

}
