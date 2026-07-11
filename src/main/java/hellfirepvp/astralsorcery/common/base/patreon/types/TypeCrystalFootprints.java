/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.base.patreon.types;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.base.patreon.FlareColor;
import hellfirepvp.astralsorcery.common.base.patreon.PatreonEffect;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.observerlib.common.util.tick.TickEvent;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TypeCrystalFootprints
 * Created by HellFirePvP
 * Date: 05.04.2020 / 10:26
 */
public class TypeCrystalFootprints extends PatreonEffect implements ITickHandler {

    private final UUID playerUUID;
    private final Color color;

    public TypeCrystalFootprints(UUID effectUUID, @Nullable FlareColor flareColor, UUID playerUUID, Color color) {
        super(effectUUID, flareColor);
        this.playerUUID = playerUUID;
        this.color = color;
    }

    @Override
    public void attachTickListeners(Consumer<ITickHandler> registrar) {
        super.attachTickListeners(registrar);

        registrar.accept(this);
    }

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        Player player = (Player) context[0];
        LogicalSide direction = (LogicalSide) context[1];

        if (direction.isClient() &&
                shouldDoEffect(player) &&
                random.nextInt(3) == 0) {

            spawnFootprint(player);
        }
    }

    private boolean shouldDoEffect(Player player) {
        return player.getUUID().equals(playerUUID) &&
                !player.isPotionActive(MobEffects.INVISIBILITY) &&
                player.onGround();
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnFootprint(Player player) {
        Vector3 pos = Vector3.atEntityCorner(player)
                .subtract(player.getBbWidth() / 2, 0.1, player.getBbWidth() / 2)
                .add(player.getBbWidth() * random.nextFloat(), 0, player.getBbWidth() * random.nextFloat());

        if (player.getCommandSenderWorld().isEmptyBlock(pos.toBlockPos())) {
            return;
        }

        EffectHelper.of(EffectTemplatesAS.CRYSTAL)
                .spawn(pos)
                .rotation(random.nextFloat() * 35F * (random.nextBoolean() ? 1 : -1),
                        random.nextFloat() * 35F * (random.nextBoolean() ? 1 : -1),
                        random.nextFloat() * 35F * (random.nextBoolean() ? 1 : -1))
                .color(VFXColorFunction.constant(this.color))
                .alpha1arg(VFXAlphaFunction.FADE_OUT)
                .setScaleMultiplier(0.025F + random.nextFloat() * 0.03F)
                .setMaxAge(60 + random.nextInt(30));
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.PLAYER);
    }

    @Override
    public boolean canFire(TickEvent.Phase currentPhase) {
        return currentPhase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "PatreonEffect - Crystal Footprints " + this.playerUUID.toString();
    }
}
