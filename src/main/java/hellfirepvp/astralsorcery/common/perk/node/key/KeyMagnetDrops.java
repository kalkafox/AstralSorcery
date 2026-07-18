/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyMagnetDrops
 * Created by HellFirePvP
 * Date: 31.08.2019 / 21:21
 */
public class KeyMagnetDrops extends KeyPerk {

    public KeyMagnetDrops(ResourceLocation name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void attachListeners(LogicalSide direction, IEventBus bus) {
        super.attachListeners(direction, bus);

        bus.addListener(EventPriority.LOW, this::onEntityLoot);
    }

    private void onEntityLoot(LivingDropsEvent event) {
        DamageSource source = event.getSource();
        if (source.getEntity() != null && source.getEntity() instanceof Player) {
            Player player = (Player) source.getEntity();
            LogicalSide direction = this.getSide(player);
            PlayerProgress prog = ResearchHelper.getProgress(player, direction);
            if (prog.getPerkData().hasPerkEffect(this)) {
                List<ItemEntity> remaining = new ArrayList<>();
                for (ItemEntity drop : event.getDrops()) {
                    ItemStack remain = ItemUtils.dropItemToPlayer(player, drop.getItem());
                    if (!remain.isEmpty()) {
                        ItemEntity newDrop = new ItemEntity(drop.getCommandSenderWorld(), drop.getX(), drop.getY(), drop.getZ(), remain);
                        newDrop.restoreFrom(drop);
                        newDrop.setItem(remain);
                        remaining.add(newDrop);
                    }
                }
                event.getDrops().clear();
                event.getDrops().addAll(remaining);
            }
        }
    }
}
