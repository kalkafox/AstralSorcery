/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.entity.InteractableEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinServerPlayNetHandler
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerPlayNetHandler {

    @Shadow public ServerPlayer player;

    //Due to compatibility, this will not be used. see reach_set_server_entity_interact.js
    /*@ModifyConstant(method = "processUseEntity", constant = @Constant(doubleValue = 36.0, ordinal = 1), require = 1)
    public double overrideEntityInteractDistanceLimit(double distance) {
        ServerGamePacketListenerImpl playNetHandler = (ServerGamePacketListenerImpl)(Object) this;
        PlayerEntity player = playNetHandler.player;

        PlayerProgress prog = ResearchHelper.getProgress(player, player.getEntityWorld().isRemote() ? LogicalSide.CLIENT : LogicalSide.SERVER);
        if (prog.isValid() && prog.getPerkData().hasPerkEffect(perk -> perk instanceof KeyEntityReach)) {
            return Double.MAX_VALUE;
        }
        return distance;
    }*/

    @Inject(
            method = "processUseEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/ServerGamePacketListenerImpl;disconnect(Lnet/minecraft/util/text/ITextComponent;)V"),
            cancellable = true
    )
    public void allowInteractableEntity(ServerboundInteractPacket packet, CallbackInfo ci) {
        ServerLevel level = this.player.serverLevel();
        Entity interacted = packet.getEntityFromWorld(level);
        if (interacted instanceof InteractableEntity) {
            this.player.attack(interacted);
            ci.cancel();
        }
    }

}
