/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.event.handler;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.GuiType;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.effect.EffectDropModifier;
import hellfirepvp.astralsorcery.common.item.ItemTome;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.lib.CapabilitiesAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.bus.api.IEventBus;
import java.util.Optional;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EventHandlerMisc
 * Created by HellFirePvP
 * Date: 23.02.2020 / 19:30
 */
//Event handler to fix/prevent lots of random stuff
public class EventHandlerMisc {

    public static void attachListeners(IEventBus bus) {
        bus.addListener(EventHandlerMisc::onSpawnEffectCloud);
        bus.addListener(EventHandlerMisc::onPlayerSleepEclipse);
        bus.addListener(EventHandlerMisc::onChunkLoad);
        bus.addListener(EventHandlerMisc::onLecternOpen);
        bus.addListener(EventHandlerMisc::onCrystalToss);
    }

    private static void onCrystalToss(ItemTossEvent event) {
        if (!event.getPlayer().getCommandSenderWorld().isClientSide()) {
            ItemStack thrown = event.getEntity().getItem();
            if (thrown.getItem() instanceof ItemCrystalBase) {
                event.getEntity().setThrower(event.getPlayer());
            }
        }
    }

    private static void onLecternOpen(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        LecternBlockEntity lectern = MiscUtils.getTileAt(event.getLevel(), event.getPos(), LecternBlockEntity.class, false);
        if (lectern != null) {
            ItemStack contained = lectern.getBook();
            if (contained.getItem() instanceof ItemTome) {
                event.setCanceled(true);
                AstralSorcery.getProxy().openGui(event.getEntity(), GuiType.TOME);
            }
        }
    }

    private static void onChunkLoad(ChunkEvent.Load event) {
        ChunkAccess ch = event.getChunk();
        if (ch instanceof LevelChunk && !event.getLevel().isClientSide()) {
            Optional.ofNullable(((LevelChunk) ch).getData(CapabilitiesAS.CHUNK_FLUID)).ifPresent(entry -> {
                if (!entry.isInitialized()) {
                    LevelAccessor w = event.getLevel();
                    if (w instanceof WorldGenLevel) {
                        long seed = ((WorldGenLevel) w).getSeed();
                        long chX = event.getChunk().getPos().x;
                        long chZ = event.getChunk().getPos().z;
                        seed ^= chX << 32;
                        seed ^= chZ;
                        entry.place(seed);
                        ((LevelChunk) ch).setUnsaved(true);
                    }
                }
            });
        }
    }

    private static void onPlayerSleepEclipse(CanPlayerSleepEvent event) {
        WorldContext ctx = SkyHandler.getContext(event.getEntity().getCommandSenderWorld());
        if (ctx != null && ctx.getCelestialEventHandler().getSolarEclipse().isActiveNow()) {
            if (event.getProblem() == null) {
                event.setProblem(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
            }
        }
    }

    private static void onSpawnEffectCloud(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof AreaEffectCloud cloud) {
            for (MobEffectInstance effect : cloud.potionContents.getAllEffects()) {
                if (effect.getEffect().value() instanceof EffectDropModifier) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }
}
