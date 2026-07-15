/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.research;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncKnowledge;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkLevelManager;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.util.MapStream;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.astralsorcery.common.util.Constants;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PlayerPerkData
 * Created by HellFirePvP
 * Date: 28.11.2020 / 15:05
 */
public class PlayerPerkData {

    private Set<ResourceLocation> freePointTokens = new HashSet<>();
    private Map<AbstractPerk, AppliedPerk> perks = new HashMap<>();
    private double perkExp = 0;

    public Collection<AbstractPerk> getSealedPerks() {
        return this.perks.values().stream()
                .filter(AppliedPerk::isSealed)
                .map(AppliedPerk::getPerk)
                .collect(Collectors.toList());
    }

    public Collection<AbstractPerk> getEffectGrantingPerks() {
        return this.perks.values().stream()
                .filter(appliedPerk -> !appliedPerk.isSealed())
                .map(AppliedPerk::getPerk)
                .collect(Collectors.toList());
    }

    public Collection<AbstractPerk> getAllocatedPerks(PerkAllocationType type) {
        return this.perks.values().stream()
                .filter(appliedPerk -> appliedPerk.isAllocated(type))
                .map(AppliedPerk::getPerk)
                .collect(Collectors.toList());
    }

    public Collection<PerkAllocationType> getAllocationTypes(AbstractPerk perk) {
        return this.findAppliedPerk(perk)
                .map(AppliedPerk::getApplicationTypes)
                .orElse(Collections.emptySet());
    }

    public boolean hasPerkEffect(Predicate<AbstractPerk> perkMatch) {
        return hasPerkAllocation(perkMatch) && !isPerkSealed(perkMatch);
    }

    public boolean hasPerkEffect(AbstractPerk perk) {
        return hasPerkAllocation(perk) && !isPerkSealed(perk);
    }

    public boolean hasPerkAllocation(Predicate<AbstractPerk> perkMatch) {
        return this.findAppliedPerk(perkMatch).isPresent();
    }

    public boolean hasPerkAllocation(AbstractPerk perk) {
        return this.findAppliedPerk(perk).isPresent();
    }

    public boolean hasPerkAllocation(AbstractPerk perk, PerkAllocationType type) {
        return this.findAppliedPerk(perk)
                .map(appliedPerk -> appliedPerk.isAllocated(type))
                .orElse(false);
    }

    protected boolean canSealPerk(AbstractPerk perk) {
        return !isPerkSealed(perk) && hasPerkAllocation(perk);
    }

    public boolean isPerkSealed(AbstractPerk perk) {
        return this.findAppliedPerk(perk)
                .map(AppliedPerk::isSealed)
                .orElse(false);
    }

    public boolean isPerkSealed(Predicate<AbstractPerk> perkMatch) {
        return this.findAppliedPerk(perkMatch)
                .map(AppliedPerk::isSealed)
                .orElse(false);
    }

    protected boolean sealPerk(AbstractPerk perk) {
        if (!canSealPerk(perk)) {
            return false;
        }
        return this.findAppliedPerk(perk)
                .map(appliedPerk -> appliedPerk.setSealed(true))
                .orElse(false);
    }

    protected boolean breakSeal(AbstractPerk perk) {
        return this.findAppliedPerk(perk)
                .filter(AppliedPerk::isSealed)
                .map(appliedPerk -> appliedPerk.setSealed(false))
                .orElse(false);
    }

    public boolean updatePerkData(AbstractPerk perk, CompoundTag data) {
        AppliedPerk appliedPerk = this.perks.get(perk);
        if (appliedPerk == null) {
            return false;
        }
        appliedPerk.perkData = data.copy();
        return true;
    }

    public boolean applyPerkAllocation(AbstractPerk perk, PlayerPerkAllocation allocation, boolean simulate) {
        if (simulate && !this.perks.containsKey(perk)) {
            return true;
        }
        AppliedPerk appliedPerk = this.perks.computeIfAbsent(perk, AppliedPerk::new);
        return appliedPerk.addAllocation(allocation, simulate);
    }

    public PerkRemovalResult removePerkAllocation(AbstractPerk perk, PlayerPerkAllocation allocation, boolean simulate) {
        AppliedPerk appliedPerk = this.perks.get(perk);
        if (appliedPerk == null) {
            return PerkRemovalResult.FAILURE;
        }
        if (appliedPerk.isAllocated(allocation.getType())) {
            PerkRemovalResult result = appliedPerk.removeAllocation(allocation, simulate);
            if (result.isFailure()) {
                return result;
            }

            if (!simulate && result == PerkRemovalResult.REMOVE_PERK) {
                this.perks.remove(perk);
            }
            return result;
        }
        return PerkRemovalResult.FAILURE;
    }

    @Nullable
    public CompoundTag getData(AbstractPerk perk) {
        return this.findAppliedPerk(perk)
                .map(AppliedPerk::getPerkData)
                .map(CompoundTag::copy)
                .orElse(null);
    }

    @Nullable
    public CompoundTag getMetaData(AbstractPerk perk) {
        return this.findAppliedPerk(perk)
                .map(AppliedPerk::getApplicationData)
                .orElse(null);
    }

    private Optional<AppliedPerk> findAppliedPerk(AbstractPerk perk) {
        return Optional.ofNullable(this.perks.get(perk));
    }

    private Optional<AppliedPerk> findAppliedPerk(Predicate<AbstractPerk> perkFilter) {
        return MapStream.of(this.perks)
                .filterKey(perkFilter)
                .valueStream()
                .findFirst();
    }

    protected boolean grantFreeAllocationPoint(ResourceLocation freePointToken) {
        if (this.freePointTokens.contains(freePointToken)) {
            return false;
        }
        this.freePointTokens.add(freePointToken);
        return true;
    }

    protected boolean tryRevokeAllocationPoint(ResourceLocation accessToken) {
        return this.freePointTokens.remove(accessToken);
    }

    public Collection<ResourceLocation> getFreePointTokens() {
        return Collections.unmodifiableCollection(this.freePointTokens);
    }

    public int getAvailablePerkPoints(Player player, LogicalSide direction) {
        int allocatedPerks = (int) this.perks.values().stream().filter(perk -> perk.isAllocated(PerkAllocationType.UNLOCKED)).count() - 1;
        int allocationLevels = PerkLevelManager.getLevel(getPerkExp(), player, direction);
        return (allocationLevels + this.freePointTokens.size()) - allocatedPerks;
    }

    public boolean hasFreeAllocationPoint(Player player, LogicalSide direction) {
        return getAvailablePerkPoints(player, direction) > 0;
    }

    public double getPerkExp() {
        return perkExp;
    }

    public int getPerkLevel(Player player, LogicalSide direction) {
        return PerkLevelManager.getLevel(getPerkExp(), player, direction);
    }

    public float getPercentToNextLevel(Player player, LogicalSide direction) {
        return PerkLevelManager.getNextLevelPercent(getPerkExp(), player, direction);
    }

    protected void modifyExp(double futureXp, Player player) {
        int currLevel = PerkLevelManager.getLevel(getPerkExp(), player, LogicalSide.SERVER);
        if (futureXp >= 0 && currLevel >= PerkLevelManager.getLevelCap(LogicalSide.SERVER, player)) {
            return;
        }
        long expThisLevel = PerkLevelManager.getExpForLevel(currLevel, player, LogicalSide.SERVER);
        long expNextLevel = PerkLevelManager.getExpForLevel(currLevel + 1, player, LogicalSide.SERVER);
        long cap = Mth.lfloor(((float) (expNextLevel - expThisLevel)) * 0.08F);
        if (futureXp > cap) {
            futureXp = cap;
        }

        this.perkExp = Math.max(this.perkExp + futureXp, 0);
    }

    protected void setExp(double futureXp) {
        this.perkExp = Math.max(futureXp, 0);
    }

    void load(PlayerProgress progress, CompoundTag tag) {
        this.perks.clear();
        this.freePointTokens.clear();
        this.perkExp = 0;

        //TODO remove with 1.17
        if (isLegacyData(tag)) {
            loadLegacyData(progress, tag);
            return;
        }

        this.perkExp = tag.getDouble("perkExp");

        long perkTreeVersion = tag.getLong("perkTreeVersion");
        if (PerkTree.PERK_TREE.getVersion(LogicalSide.SERVER).map(v -> !v.equals(perkTreeVersion)).orElse(true)) { //If your perk tree is different, clear it.
            AstralSorcery.log.info("Clearing perk-tree because the player's skill-tree version was outdated!");
            if (progress.getAttunedConstellation() != null) {
                AbstractPerk root = PerkTree.PERK_TREE.getRootPerk(LogicalSide.SERVER, progress.getAttunedConstellation());
                if (root != null) {
                    AppliedPerk newPerk = new AppliedPerk(root);
                    newPerk.addAllocation(PlayerPerkAllocation.unlock(), false);
                    root.onUnlockPerkServer(null, PerkAllocationType.UNLOCKED, progress, newPerk.getPerkData());
                    this.perks.put(root, newPerk);
                }
            }
            return;
        }

        //TODO Remove .replace("-", "_") in 1.17
        this.freePointTokens.addAll(NBTHelper.readList(tag, "tokens", Constants.NBT.TAG_STRING,
                nbt -> ResourceLocation.parse(nbt.getAsString().replace("-", "_"))));

        ListTag list = tag.getList("perks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag nbt = list.getCompound(i);
            AppliedPerk.deserialize(nbt).ifPresent(perk -> this.perks.put(perk.getPerk(), perk));
        }
    }

    void save(CompoundTag tag) {
        PerkTree.PERK_TREE.getVersion(LogicalSide.SERVER)
                .ifPresent(version -> tag.putLong("perkTreeVersion", version));
        tag.putDouble("perkExp", this.perkExp);

        ListTag tokens = new ListTag();
        for (ResourceLocation key : this.freePointTokens) {
            tokens.add(StringTag.valueOf(key.toString()));
        }
        tag.put("tokens", tokens);

        ListTag perks = new ListTag();
        for (AppliedPerk perk : this.perks.values()) {
            perks.add(perk.serialize());
        }
        tag.put("perks", perks);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeDouble(this.perkExp);
        ByteBufUtils.writeCollection(buf, this.freePointTokens, ByteBufUtils::writeResourceLocation);
        ByteBufUtils.writeCollection(buf, this.perks.values(), (buffer, perk) -> {
            ByteBufUtils.writeResourceLocation(buffer, perk.getPerk().getRegistryName());
            perk.write(buffer);
        });
    }

    public static PlayerPerkData read(FriendlyByteBuf buf, LogicalSide direction) {
        PlayerPerkData data = new PlayerPerkData();
        data.perkExp = buf.readDouble();
        data.freePointTokens = ByteBufUtils.readSet(buf, ByteBufUtils::readResourceLocation);
        Set<AppliedPerk> appliedPerks = ByteBufUtils.readSet(buf, buffer -> {
            ResourceLocation key = ByteBufUtils.readResourceLocation(buffer);
            return PerkTree.PERK_TREE.getPerk(direction, key)
                    .map(AppliedPerk::new)
                    .map(perk -> {
                        perk.read(buffer);
                        return perk;
                    })
                    .orElseThrow(() -> new IllegalArgumentException("Unknown perk: " + key));
        });
        appliedPerks.forEach(appliedPerk -> data.perks.put(appliedPerk.getPerk(), appliedPerk));
        return data;
    }

    @OnlyIn(Dist.CLIENT)
    void receive(PktSyncKnowledge message) {
        PlayerPerkData copyFrom = message.perkData;

        this.perkExp = copyFrom.perkExp;
        this.freePointTokens = copyFrom.freePointTokens;
        this.perks = copyFrom.perks;
    }

    private boolean isLegacyData(CompoundTag tag) {
        return tag.contains("sealedPerks");
    }

    private void loadLegacyData(PlayerProgress progress, CompoundTag pattern) {
        long perkTreeLevel = pattern.getLong("perkTreeVersion");
        if (PerkTree.PERK_TREE.getVersion(LogicalSide.SERVER).map(v -> !v.equals(perkTreeLevel)).orElse(true)) { //If your perk tree is different, clear it.
            AstralSorcery.log.info("Clearing perk-tree because the player's skill-tree version was outdated!");
            if (progress.getAttunedConstellation() != null) {
                AbstractPerk root = PerkTree.PERK_TREE.getRootPerk(LogicalSide.SERVER, progress.getAttunedConstellation());
                if (root != null) {
                    AppliedPerk newPerk = new AppliedPerk(root);
                    newPerk.addAllocation(PlayerPerkAllocation.unlock(), false);
                    root.onUnlockPerkServer(null, PerkAllocationType.UNLOCKED, progress, newPerk.getPerkData());
                    this.perks.put(root, newPerk);
                }
            }
        } else {
            if (pattern.contains("perks")) {
                ListTag list = pattern.getList("perks", Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag tag = list.getCompound(i);
                    String perkRegName = tag.getString("perkName");
                    CompoundTag data = tag.getCompound("perkData");
                    PerkTree.PERK_TREE.getPerk(LogicalSide.SERVER, ResourceLocation.parse(perkRegName)).ifPresent(perk -> {
                        AppliedPerk appliedPerk = new AppliedPerk(perk);
                        appliedPerk.addAllocation(PlayerPerkAllocation.unlock(), false);
                        appliedPerk.perkData = data;
                        this.perks.put(perk, appliedPerk);
                    });
                }
            }
            if (pattern.contains("sealedPerks")) {
                ListTag list = pattern.getList("sealedPerks", Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag tag = list.getCompound(i);
                    String perkRegName = tag.getString("perkName");
                    PerkTree.PERK_TREE.getPerk(LogicalSide.SERVER, ResourceLocation.parse(perkRegName)).ifPresent(perk -> {
                        AppliedPerk newPerk = this.perks.get(perk);
                        if (newPerk != null) {
                            newPerk.setSealed(true);
                        }
                    });
                }
            }

            if (pattern.contains("pointTokens")) {
                ListTag list = pattern.getList("pointTokens", Constants.NBT.TAG_STRING);
                for (int i = 0; i < list.size(); i++) {
                    String[] resource = legacySplitKey(list.getString(i).toLowerCase(Locale.ROOT));
                    resource[1] = resource[1].replace("-", "_").replace(":", "_");
                    this.freePointTokens.add(AstralSorcery.key(resource[1]));
                }
            }
        }

        if (pattern.contains("perkExp")) {
            this.perkExp = pattern.getDouble("perkExp");
        }
    }

    private static String[] legacySplitKey(String resource) {
        String[] keyParts = new String[]{ "minecraft", resource };
        int i = resource.indexOf(":");
        if (i >= 0) {
            keyParts[1] = resource.substring(i + 1);
        }
        return keyParts;
    }

    public static class AppliedPerk {

        private static final String SEALED_KEY = "sealed";
        private static final String APPLICATION_KEYS = "application";

        private final AbstractPerk perk;
        private CompoundTag perkData = new CompoundTag();
        private CompoundTag applicationData = new CompoundTag();
        private Set<PerkAllocationType> applicationTypes = new HashSet<>();

        public AppliedPerk(AbstractPerk perk) {
            this.perk = perk;
        }

        public boolean isSealed() {
            return this.applicationData.contains(SEALED_KEY);
        }

        public boolean setSealed(boolean sealed) {
            if (sealed) {
                this.applicationData.putBoolean(SEALED_KEY, true);
            } else {
                this.applicationData.remove(SEALED_KEY);
            }
            return true;
        }

        public AbstractPerk getPerk() {
            return this.perk;
        }

        public CompoundTag getPerkData() {
            return this.perkData;
        }

        public CompoundTag getApplicationData() {
            return this.applicationData;
        }

        private int getTotalAllocationCount() {
            int sum = 0;
            for (PerkAllocationType type : PerkAllocationType.values()) {
                sum += getAllocationCount(type);
            }
            return sum;
        }

        private int getAllocationCount(PerkAllocationType type) {
            CompoundTag metaData = this.getApplicationData();
            if (!metaData.contains(APPLICATION_KEYS, Constants.NBT.TAG_COMPOUND)) {
                return 0;
            }
            CompoundTag applicationMeta = metaData.getCompound(APPLICATION_KEYS);
            ListTag allocations = applicationMeta.getList(type.getSaveKey(), Constants.NBT.TAG_COMPOUND);
            return allocations.size();
        }

        public boolean isAllocated(PerkAllocationType type) {
            return this.applicationTypes.contains(type);
        }

        private PerkRemovalResult removeAllocation(PlayerPerkAllocation type, boolean simulate) {
            CompoundTag metaData = this.getApplicationData();
            if (!metaData.contains(APPLICATION_KEYS, Constants.NBT.TAG_COMPOUND)) {
                return PerkRemovalResult.FAILURE;
            }
            CompoundTag applicationMeta = metaData.getCompound(APPLICATION_KEYS);
            ListTag allocations = applicationMeta.getList(type.getType().getSaveKey(), Constants.NBT.TAG_COMPOUND);
            if (allocations.isEmpty()) {
                return PerkRemovalResult.FAILURE;
            }

            boolean removedMatch = false;
            UUID removeUUID = type.getLockUUID();
            for (int i = 0; i < allocations.size(); i++) {
                CompoundTag tag = allocations.getCompound(i);
                UUID lockUUID = tag.getUUID("uuid");
                if (lockUUID.equals(removeUUID)) {
                    if (!simulate) {
                        allocations.remove(i);
                    }
                    removedMatch = true;
                    break;
                }
            }
            if (!removedMatch) {
                return PerkRemovalResult.FAILURE;
            }

            if (simulate && allocations.size() <= 1) {
                if (this.applicationTypes.size() > 1) {
                    return PerkRemovalResult.REMOVE_ALLOCATION_TYPE;
                } else {
                    return PerkRemovalResult.REMOVE_PERK;
                }
            }
            if (allocations.isEmpty()) {
                this.applicationTypes.remove(type.getType());
                if (this.applicationTypes.isEmpty()) {
                    return PerkRemovalResult.REMOVE_PERK;
                }
                return PerkRemovalResult.REMOVE_ALLOCATION_TYPE;
            }
            return PerkRemovalResult.REMOVE_ALLOCATION;
        }

        public boolean addAllocation(PlayerPerkAllocation type, boolean simulate) {
            if (!simulate) {
                this.applicationTypes.add(type.getType());
            }

            CompoundTag metaData = this.getApplicationData();
            if (!metaData.contains(APPLICATION_KEYS, Constants.NBT.TAG_COMPOUND)) {
                if (simulate) {
                    return true;
                }
                metaData.put(APPLICATION_KEYS, new CompoundTag());
            }
            CompoundTag applicationMeta = metaData.getCompound(APPLICATION_KEYS);

            String key = type.getType().getSaveKey();
            if (!applicationMeta.contains(key, Constants.NBT.TAG_LIST)) {
                if (simulate) {
                    return true;
                }
                applicationMeta.put(key, new ListTag());
            }
            ListTag allocations = applicationMeta.getList(key, Constants.NBT.TAG_COMPOUND);

            UUID newUUID = type.getLockUUID();
            CompoundTag newKeyTag = new CompoundTag();
            newKeyTag.putUUID("uuid", newUUID);

            if (allocations.isEmpty()) {
                if (!simulate) {
                    allocations.add(newKeyTag);
                }
                return true;
            }
            for (int i = 0; i < allocations.size(); i++) {
                CompoundTag tag = allocations.getCompound(i);
                UUID lockUUID = tag.getUUID("uuid");
                if (lockUUID.equals(newUUID)) {
                    return false;
                }
            }
            if (simulate) {
                return true;
            }
            return allocations.add(newKeyTag);
        }

        public Set<PerkAllocationType> getApplicationTypes() {
            return this.applicationTypes;
        }

        private CompoundTag serialize() {
            CompoundTag out = new CompoundTag();
            out.putString("perk", this.perk.getRegistryName().toString());
            out.put("perkData", this.perkData);
            out.put("applicationData", this.applicationData);
            int[] types = this.applicationTypes.stream()
                    .mapToInt(Enum::ordinal)
                    .toArray();
            out.putIntArray("applicationTypes", types);
            return out;
        }

        private static Optional<AppliedPerk> deserialize(CompoundTag tag) {
            ResourceLocation key = ResourceLocation.parse(tag.getString("perk"));
            return PerkTree.PERK_TREE.getPerk(LogicalSide.SERVER, key)
                    .map(AppliedPerk::new)
                    .map(appliedPerk -> {
                        appliedPerk.perkData = tag.getCompound("perkData");
                        appliedPerk.applicationData = tag.getCompound("applicationData");
                        int[] types = tag.getIntArray("applicationTypes");
                        appliedPerk.applicationTypes = IntStream.of(types)
                                .mapToObj(type -> PerkAllocationType.values()[type])
                                .collect(Collectors.toSet());
                        return appliedPerk;
                    });
        }

        private void write(FriendlyByteBuf buf) {
            ByteBufUtils.writeNBTTag(buf, this.perkData);
            ByteBufUtils.writeNBTTag(buf, this.applicationData);
            ByteBufUtils.writeCollection(buf, this.applicationTypes, ByteBufUtils::writeEnumValue);
        }

        private void read(FriendlyByteBuf buf) {
            this.perkData = ByteBufUtils.readNBTTag(buf);
            this.applicationData = ByteBufUtils.readNBTTag(buf);
            this.applicationTypes = ByteBufUtils.readSet(buf, buffer -> ByteBufUtils.readEnumValue(buffer, PerkAllocationType.class));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AppliedPerk that = (AppliedPerk) o;
            return Objects.equals(perk.getRegistryName(), that.perk.getRegistryName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(perk.getRegistryName());
        }
    }
}
