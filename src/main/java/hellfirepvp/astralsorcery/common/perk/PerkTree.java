/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.perk.data.PerkTreeData;
import hellfirepvp.astralsorcery.common.perk.data.PreparedPerkTreeData;
import hellfirepvp.astralsorcery.common.perk.node.RootPerk;
import hellfirepvp.astralsorcery.common.perk.tree.PerkTreePoint;
import hellfirepvp.astralsorcery.common.util.SidedReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PerkTree
 * Created by HellFirePvP
 * Date: 02.06.2019 / 08:31
 */
public class PerkTree {

    public static final PerkTree PERK_TREE = new PerkTree();

    //The original tree, loaded from JSON on the server
    private PerkTreeData loadedPerkTree = null;

    private final SidedReference<PreparedPerkTreeData> treeData = new SidedReference<>();

    private PerkTree() {}

    public Optional<PreparedPerkTreeData> getData(LogicalSide direction) {
        return this.treeData.getData(direction);
    }

    public Optional<AbstractPerk> getPerk(LogicalSide direction, ResourceLocation key) {
        return this.getPerk(direction, perk -> key.equals(perk.getRegistryName()));
    }

    public Optional<AbstractPerk> getPerk(LogicalSide direction, Predicate<AbstractPerk> test) {
        return this.getData(direction).flatMap(data -> data.getPerk(test));
    }

    public Optional<? extends AbstractPerk> getPerk(LogicalSide direction, float x, float y) {
        return this.getData(direction).flatMap(data -> data.getPerk(x, y));
    }

    @Nullable
    public RootPerk getRootPerk(LogicalSide direction, IConstellation constellation) {
        return this.getData(direction).map(data -> data.getRootPerk(constellation)).orElse(null);
    }

    public Collection<AbstractPerk> getConnectedPerks(LogicalSide direction, AbstractPerk perk) {
        return this.getData(direction).map(data -> data.getConnectedPerks(perk)).orElse(Collections.emptyList());
    }

    public Collection<PerkTreePoint<?>> getPerkPoints(LogicalSide direction) {
        return this.getData(direction).map(PreparedPerkTreeData::getPerkPoints).orElse(Collections.emptyList());
    }

    //Only for rendering purposes.
    @OnlyIn(Dist.CLIENT)
    public Collection<Tuple<AbstractPerk, AbstractPerk>> getConnections() {
        return this.getData(LogicalSide.CLIENT).map(PreparedPerkTreeData::getConnections).orElse(Collections.emptyList());
    }

    public Optional<Long> getVersion(LogicalSide direction) {
        return this.getData(direction).map(PreparedPerkTreeData::getVersion);
    }

    public void updateOriginPerkTree(PerkTreeData perkTree) {
        this.loadedPerkTree = perkTree;
    }

    public Optional<Collection<JsonObject>> getLoginPerkData() {
        return Optional.ofNullable(this.loadedPerkTree).map(PerkTreeData::getAsDataTree);
    }

    @OnlyIn(Dist.CLIENT)
    public void receivePerkTree(PreparedPerkTreeData serverTreeData) {
        this.updateTreeData(LogicalSide.CLIENT, serverTreeData);
    }

    public void clearCache(LogicalSide direction) {
        this.getData(direction).ifPresent(data -> data.clearPerkCache(direction));
        this.updateTreeData(direction, null);
    }

    public void setupServerPerkTree() {
        if (this.loadedPerkTree != null) {
            this.updateTreeData(LogicalSide.SERVER, this.loadedPerkTree.prepare());
            AstralSorcery.log.info("Loaded PerkTree!");
        } else {
            AstralSorcery.log.info("No PerkTree data found!");
        }
    }

    private void updateTreeData(LogicalSide direction, @Nullable PreparedPerkTreeData newData) {
        this.treeData.getData(direction).ifPresent(data -> {
            data.getPerkPoints().stream()
                    .map(PerkTreePoint::getPerk)
                    .forEach(perk -> perk.invalidate(direction));
        });
        this.treeData.setData(direction, newData);
        if (newData != null) {
            newData.getPerkPoints().stream()
                    .map(PerkTreePoint::getPerk)
                    .forEach(perk -> perk.validate(direction));
        }
    }
}
