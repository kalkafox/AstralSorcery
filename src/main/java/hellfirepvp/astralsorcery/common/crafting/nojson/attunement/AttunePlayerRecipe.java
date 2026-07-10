/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.nojson.attunement;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.active.ActivePlayerAttunementRecipe;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.tile.TileAttunementAltar;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.entity.EntityUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AttunePlayerRecipe
 * Created by HellFirePvP
 * Date: 02.12.2019 / 19:37
 */
public class AttunePlayerRecipe extends AttunementRecipe<ActivePlayerAttunementRecipe> {

    private static final AABB BOX = new AABB(0, 0, 0, 1, 1, 1);

    public AttunePlayerRecipe() {
        super(AstralSorcery.key("attune_player"));
    }

    @Override
    public boolean canStartCrafting(TileAttunementAltar altar) {
        Level level = altar.getLevel();
        if (DayTimeHelper.isNight(level)) {
            return findEligiblePlayer(altar) != null;
        }
        return false;
    }

    @Override
    @Nonnull
    public ActivePlayerAttunementRecipe createRecipe(TileAttunementAltar altar) {
        ServerPlayer player = findEligiblePlayer(altar);
        return new ActivePlayerAttunementRecipe(this, (IMajorConstellation) altar.getActiveConstellation(), player.getUUID());
    }

    @Override
    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public ActivePlayerAttunementRecipe deserialize(TileAttunementAltar altar, CompoundTag nbt, @Nullable ActivePlayerAttunementRecipe previousInstance) {
        ActivePlayerAttunementRecipe recipe = new ActivePlayerAttunementRecipe(this, nbt);
        if (previousInstance != null) {
            recipe.cameraHack = previousInstance.cameraHack;
        }
        return recipe;
    }

    @Nullable
    private static ServerPlayer findEligiblePlayer(TileAttunementAltar altar) {
        if (!(altar.getActiveConstellation() instanceof IMajorConstellation)) {
            return null;
        }
        AABB boxAt = BOX.offset(altar.getBlockPos().above()).grow(1);

        Vector3 thisVec = new Vector3(altar).add(0.5, 1.5, 0.5);
        List<ServerPlayer> players = altar.getLevel().getEntitiesWithinAABB(ServerPlayer.class, boxAt);
        if (!players.isEmpty()) {
            ServerPlayer pl = EntityUtils.selectClosest(players, (player) -> thisVec.distanceSquared(player.position()));
            if (isEligablePlayer(pl, altar.getActiveConstellation())) {
                return pl;
            }
        }
        return null;
    }

    public static boolean isEligablePlayer(ServerPlayer player, IConstellation attuneTo) {
        if (player != null && player.isAlive() && !MiscUtils.isPlayerFakeMP(player) && !player.isShiftKeyDown()) {
            PlayerProgress prog = ResearchHelper.getProgress(player, LogicalSide.SERVER);

            return prog.isValid() &&
                    attuneTo instanceof IMajorConstellation &&
                    !prog.isAttuned() &&
                    prog.getTierReached().isThisLaterOrEqual(ProgressionTier.ATTUNEMENT) &&
                    prog.hasConstellationDiscovered(attuneTo);
        }
        return false;
    }
}
