/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.source.provider;

import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.perk.source.ModifierManager;
import hellfirepvp.astralsorcery.common.perk.source.ModifierSourceProvider;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PerkSourceProvider
 * Created by HellFirePvP
 * Date: 01.04.2020 / 19:32
 */
public class PerkSourceProvider extends ModifierSourceProvider<AbstractPerk> {

    public PerkSourceProvider() {
        super(ModifierManager.PERK_PROVIDER_KEY);
    }

    @Override
    protected void update(ServerPlayer playerEntity) {}

    @Override
    protected void removeModifiers(ServerPlayer playerEntity) {}

    @Override
    public void serialize(AbstractPerk source, FriendlyByteBuf buf) {
        ByteBufUtils.writeResourceLocation(buf, source.getRegistryName());
    }

    @Override
    public AbstractPerk deserialize(FriendlyByteBuf buf) {
        ResourceLocation perkKey = ByteBufUtils.readResourceLocation(buf);
        return PerkTree.PERK_TREE.getPerk(LogicalSide.CLIENT, perkKey).orElse(null);
    }
}
