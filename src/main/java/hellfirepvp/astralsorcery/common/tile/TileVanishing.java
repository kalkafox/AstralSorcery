/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.TileEntityTick;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileVanishing
 * Created by HellFirePvP
 * Date: 11.03.2020 / 21:16
 */
public class TileVanishing extends TileEntityTick {

    private static final AABB SEARCH_BOX = new AABB(-4,0, -4, 4, 3, 4);

    public TileVanishing(BlockPos pos, BlockState state) {
        super(TileEntityTypesAS.VANISHING, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getLevel().isClientSide() && this.getTicksExisted() % 5 == 0) {
            boolean removeBlock = true;

            List<Player> players = getLevel().getEntitiesWithinAABB(Player.class, SEARCH_BOX.offset(getBlockPos()));
            for (Player player : players) {
                if (ItemMantle.getEffect(player, ConstellationsAS.aevitas) != null) {
                    double yDiff = player.getY() - this.getBlockPos().getY();

                    //Standing on top of this block
                    if (player.onGround() && yDiff >= 0.95 && yDiff <= 1.15) {
                        if (player.isShiftKeyDown()) { //Indicating they want to drop down
                            break; //Remove the block
                        }

                        removeBlock = false;
                    } else if (player.isShiftKeyDown() && yDiff >= 0.95 && yDiff <= 2.15) {
                        removeBlock = false;
                    }
                }
            }

            if (removeBlock) {
                this.getLevel().removeBlock(getBlockPos(), false);
            }
        }

        if (this.getLevel().isClientSide()) {
            this.tickClient();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickClient() {
        for (int i = 0; i < 3; i++) {
            if (random.nextFloat() < 0.07F) {
                Vector3 at = new Vector3(pos).add(0.5F, 0.5F, 0.5F).add(Vector3.random());
                FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(at)
                        .setScaleMultiplier(0.15F + random.nextFloat() * 0.1F)
                        .alpha1arg(VFXAlphaFunction.PYRAMID)
                        .setMaxAge(40 + random.nextInt(10));
                if (random.nextBoolean()) {
                    p.color(VFXColorFunction.WHITE);
                } else {
                    p.color(VFXColorFunction.constant(ColorsAS.RITUAL_CONSTELLATION_AEVITAS));
                }
            }
        }
    }
}
