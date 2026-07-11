/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.client.effect.EntityVisualFX;
import hellfirepvp.astralsorcery.client.effect.function.RefreshFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXColorEffectSphere;
import hellfirepvp.astralsorcery.client.event.effect.GatewayUIRenderHandler;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.auxiliary.link.LinkableTileEntity;
import hellfirepvp.astralsorcery.common.data.world.GatewayCache;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.DataAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import hellfirepvp.astralsorcery.common.tile.base.TileEntityTick;
import hellfirepvp.astralsorcery.common.tile.base.TileOwned;
import hellfirepvp.astralsorcery.common.util.ColorUtils;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.PlayerReference;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Nameable;
import net.minecraft.util.Tuple;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileCelestialGateway
 * Created by HellFirePvP
 * Date: 10.09.2020 / 17:07
 */
public class TileCelestialGateway extends TileEntityTick implements Nameable, TileOwned, LinkableTileEntity {

    private static final BlockPos[] OFFSETS_ALLOWED_PREVIEW = new BlockPos[] {
            new BlockPos(-3, 0, -2),
            new BlockPos(-3, 0, -1),
            new BlockPos(-3, 0,  0),
            new BlockPos(-3, 0,  1),
            new BlockPos(-3, 0,  2),

            new BlockPos(-2, 0,  3),
            new BlockPos(-1, 0,  3),
            new BlockPos( 0, 0,  3),
            new BlockPos( 1, 0,  3),
            new BlockPos( 2, 0,  3),

            new BlockPos( 3, 0,  2),
            new BlockPos( 3, 0,  1),
            new BlockPos( 3, 0,  0),
            new BlockPos( 3, 0, -1),
            new BlockPos( 3, 0, -2),

            new BlockPos( 2, 0, -3),
            new BlockPos( 1, 0, -3),
            new BlockPos( 0, 0, -3),
            new BlockPos(-1, 0, -3),
            new BlockPos(-2, 0, -3)
    };

    private boolean networkRegistered = false;
    private Component chatComponent = null;
    private DyeColor color = null;

    private boolean locked = false;
    private PlayerReference owner = null;
    private final Map<Integer, PlayerReference> allowedUsers = new HashMap<>();

    private Object clientGatewaySphereEffect = null;

    public TileCelestialGateway(BlockPos pos, BlockState state) {
        super(TileEntityTypesAS.GATEWAY, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide()) {
            playEffects();
        } else {
            boolean complete = this.hasMultiblock() & this.doesSeeSky();
            if (complete) {
                if (!networkRegistered) {
                    GatewayCache cache = DataAS.DOMAIN_AS.getData(level, DataAS.KEY_GATEWAY_CACHE);
                    if (cache.offerPosition(level, getBlockPos())) {
                        cache.updateGatewayNode(getBlockPos(), node -> {
                            node.setLastHealthTime(this.chatComponent);
                            node.setColor(this.color);
                        });
                        this.updateAccessInformation();
                        networkRegistered = true;
                        markForUpdate();
                    }
                }
            } else if (networkRegistered) {
                DataAS.DOMAIN_AS.getData(level, DataAS.KEY_GATEWAY_CACHE).removePosition(level, getBlockPos());
                networkRegistered = false;
                markForUpdate();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playEffects() {
        setupGatewaySphere();
        playGatewayParticles();
    }

    @OnlyIn(Dist.CLIENT)
    private void setupGatewaySphere() {
        if (!this.hasMultiblock() || !this.doesSeeSky()) {
            if (clientGatewaySphereEffect != null) {
                ((EntityVisualFX) clientGatewaySphereEffect).requestRemoval();
                clientGatewaySphereEffect = null;
            }
            return;
        }

        Vector3 at = new Vector3(this).add(0.5, 1.7, 0.5);
        double distance = Vector3.atEntityCorner(Minecraft.getInstance().player).distance(at);
        if (clientGatewaySphereEffect == null) {
            clientGatewaySphereEffect = EffectHelper.of(EffectTemplatesAS.COLOR_SPHERE)
                    .spawn(at)
                    .setupSphere(Vector3.RotAxis.Y_AXIS, 6)
                    .setRemoveIfInvisible(true)
                    .setAlphaFadeDistance(4)
                    .setAlphaMultiplier(1)
                    .color(VFXColorFunction.BLACK)
                    .refresh(RefreshFunction.tileExistsAnd(this, (te, fx) -> te.doesSeeSky() && te.hasMultiblock()));
        } else if (((EntityVisualFX) clientGatewaySphereEffect).isRemoved() && distance < 5) {
            EffectHelper.refresh(((FXColorEffectSphere) clientGatewaySphereEffect), EffectTemplatesAS.COLOR_SPHERE);
        }

        if (distance < 5.5) {
            Minecraft.getInstance().options.setPointOfView(CameraType.FIRST_PERSON);
        }
        if (distance < 2.5) {
            GatewayUIRenderHandler.getInstance().getOrCreateUI(this.getLevel(), this.getBlockPos(), at);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playGatewayParticles() {
        if (!this.hasMultiblock() || !this.doesSeeSky()) {
            return;
        }

        Color gatewayColor = ColorUtils.flareColorFromDye(this.getColor().orElse(DyeColor.YELLOW));
        for (int i = 0; i < 3; i++) {
            Vector3 offset = new Vector3(this).add(-2, 0.05, -2);
            if (random.nextBoolean()) {
                offset.add(5 * (random.nextBoolean() ? 1 : 0), 0, random.nextFloat() * 5);
            } else {
                offset.add(random.nextFloat() * 5, 0, 5 * (random.nextBoolean() ? 1 : 0));
            }

            Color c = MiscUtils.eitherOf(random, Color.WHITE, gatewayColor, gatewayColor.brighter());
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(offset)
                    .setGravityStrength(-0.0001F)
                    .color(VFXColorFunction.constant(c))
                    .setScaleMultiplier(0.25F + random.nextFloat() * 0.15F)
                    .setMaxAge(30 + random.nextInt(30));
        }
        for (int i = 0; i < 2; i++) {
            Vector3 offset = new Vector3();
            MiscUtils.applyRandomOffset(offset, random, 3F);
            offset.add(new Vector3(this)).add(0.5, 0, 0.5).setY(this.getBlockPos().getY() + 0.05);

            Color c = MiscUtils.eitherOf(random, Color.WHITE, gatewayColor, gatewayColor.brighter());
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(offset)
                    .setGravityStrength(-0.00004F)
                    .color(VFXColorFunction.constant(c))
                    .setScaleMultiplier(0.15F + random.nextFloat() * 0.1F)
                    .setMaxAge(15 + random.nextInt(10));
        }

        if (this.isLocked() && this.getOwner() != null) {
            Vector3 center = new Vector3(this).add(0.5, 0.2, 0.5);
            for (int i = 0; i < random.nextInt(5) + 2; i++) {
                Vector3 pos = MiscUtils.getRandomCirclePosition(center, Vector3.RotAxis.Y_AXIS, 1.7);
                MiscUtils.applyRandomOffset(pos, random, 0.05F);
                Color c = MiscUtils.eitherOf(random, Color.WHITE, ColorsAS.EFFECT_BLUE_LIGHT, ColorsAS.EFFECT_BLUE_DARK);
                EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(pos)
                        .color(VFXColorFunction.constant(c))
                        .setScaleMultiplier(0.25F + random.nextFloat() * 0.15F)
                        .alpha1arg(VFXAlphaFunction.FADE_OUT)
                        .setMaxAge(15 + random.nextInt(10));
            }
            for (int i = 0; i < random.nextInt(3) + 1; i++) {
                Vector3 pos = MiscUtils.getRandomCirclePosition(center, Vector3.RotAxis.Y_AXIS, 1.1).addY(0.3);
                MiscUtils.applyRandomOffset(pos, random, 0.05F);
                Color c = MiscUtils.eitherOf(random, Color.WHITE, ColorsAS.EFFECT_BLUE_LIGHT, ColorsAS.EFFECT_BLUE_DARK);
                EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(pos)
                        .color(VFXColorFunction.constant(c))
                        .setScaleMultiplier(0.25F + random.nextFloat() * 0.15F)
                        .alpha1arg(VFXAlphaFunction.FADE_OUT)
                        .setMaxAge(15 + random.nextInt(10));
            }
        }
    }

    public boolean isLocked() {
        return this.locked;
    }

    public boolean lock() {
        if (this.isLocked() || this.getOwner() == null) {
            return false;
        }
        this.locked = true;
        this.updateAccessInformation();
        return true;
    }

    public boolean unlock() {
        if (!this.isLocked()) {
            return false;
        }
        this.locked = false;
        this.updateAccessInformation();
        return true;
    }

    @Nullable
    @Override
    public PlayerReference getOwner() {
        return this.owner;
    }

    @Nullable
    @Override
    public PlayerReference setOwner(@Nullable PlayerReference player) {
        PlayerReference prevOwner = this.owner;
        this.owner = player;
        this.updateAccessInformation();
        return prevOwner;
    }

    public boolean canAddAllowedUser(Player otherUser) {
        return this.canAddAllowedUser(PlayerReference.of(otherUser));
    }

    public boolean canAddAllowedUser(PlayerReference otherUser) {
        if (this.getOwner() == null) {
            return false;
        }
        if (this.allowedUsers.size() >= OFFSETS_ALLOWED_PREVIEW.length) {
            return false;
        }
        return !this.allowedUsers.containsValue(otherUser);
    }

    public boolean addAllowedUser(Player otherUser) {
        return this.addAllowedUser(PlayerReference.of(otherUser));
    }

    public boolean addAllowedUser(PlayerReference otherUser) {
        if (!this.canAddAllowedUser(otherUser)) {
            return false;
        }
        List<Integer> availableIndices = new ArrayList<>();
        for (int i = 0; i < OFFSETS_ALLOWED_PREVIEW.length; i++) {
            if (!this.allowedUsers.containsKey(i)) {
                availableIndices.add(i);
            }
        }
        if (availableIndices.isEmpty()) {
            return false;
        }

        Collections.shuffle(availableIndices);
        this.allowedUsers.put(MiscUtils.getRandomEntry(availableIndices, random), otherUser);
        this.updateAccessInformation();
        return true;
    }

    @Nullable
    public PlayerReference removeAllowedUser(UUID otherUser) {
        if (this.allowedUsers.isEmpty()) {
            return null;
        }
        for (Map.Entry<Integer, PlayerReference> entry : this.allowedUsers.entrySet()) {
            if (entry.getValue().getPlayerUUID().equals(otherUser)) {
                this.allowedUsers.remove(entry.getKey());
                this.updateAccessInformation();
                return entry.getValue();
            }
        }
        return null;
    }

    public Map<Integer, PlayerReference> getAllowedUsers() {
        if (this.getOwner() == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(this.allowedUsers);
    }

    private void updateAccessInformation() {
        DataAS.DOMAIN_AS.getData(level, DataAS.KEY_GATEWAY_CACHE).updateGatewayNode(this.getBlockPos(), node -> {
            node.setLocked(this.isLocked());
            node.setOwner(this.getOwner());
            node.setAllowedUsers(this.allowedUsers);
        });
        this.markForUpdate();
    }

    @OnlyIn(Dist.CLIENT)
    public static void playAccessRevokeEffect(PktPlayEffect pktPlayEffect) {

    }

    public static BlockPos getAllowedUserOffset(int index) {
        return OFFSETS_ALLOWED_PREVIEW[Mth.clamp(index, 0, OFFSETS_ALLOWED_PREVIEW.length - 1)];
    }

    public void setDisplayText(@Nullable Component chatComponent) {
        this.chatComponent = chatComponent;
    }

    public void setColor(@Nullable DyeColor color) {
        this.color = color;
    }

    @Nullable
    @Override
    public StructureType getRequiredStructureType() {
        return StructureTypesAS.PTYPE_CELESTIAL_GATEWAY;
    }

    @Override
    public boolean seesSkyInNoSkyWorlds() {
        return true;
    }

    @Override
    public Component getName() {
        return this.chatComponent != null ? this.chatComponent : Component.translatable("block.astralsorcery.celestial_gateway");
    }

    @Override
    public boolean hasCustomName() {
        return this.chatComponent != null;
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return getName();
    }

    public Optional<DyeColor> getColor() {
        return Optional.ofNullable(this.color);
    }

    @Override
    public void readCustomNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.readCustomNBT(pattern, registries);

        this.networkRegistered = pattern.getBoolean("networkRegistered");
        this.chatComponent = pattern.contains("displayText") ? Component.Serializer.getComponentFromJson(pattern.getString("displayText")) : null;
        this.color = pattern.contains("color") ? NBTHelper.readEnum(pattern, "color", DyeColor.class) : null;

        this.locked = pattern.getBoolean("locked");
        this.owner = NBTHelper.readOptional(pattern, "owningPlayer", PlayerReference::deserialize);
        this.allowedUsers.clear();
        NBTHelper.readList(pattern, "allowedUsers", Constants.NBT.TAG_COMPOUND, nbt -> {
            CompoundTag tag = (CompoundTag) nbt;
            return new Tuple<>(tag.getInt("index"), PlayerReference.deserialize(tag.getCompound("player")));
        }).forEach(tpl -> this.allowedUsers.put(tpl.getA(), tpl.getB()));
    }

    @Override
    public void writeCustomNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.writeCustomNBT(pattern, registries);

        pattern.putBoolean("networkRegistered", this.networkRegistered);
        if (this.chatComponent != null) {
            pattern.putString("displayText", Component.Serializer.getPos(this.chatComponent));
        }
        if (this.color != null) {
            NBTHelper.writeEnum(pattern, "color", this.color);
        }

        pattern.putBoolean("locked", this.locked);
        NBTHelper.writeOptional(pattern, "owningPlayer", this.owner, (tag, playerRef) -> playerRef.save(tag));
        NBTHelper.writeList(pattern, "allowedUsers", this.allowedUsers.entrySet(), entry -> {
            CompoundTag tag = new CompoundTag();
            tag.putInt("index", entry.getKey());
            tag.put("player", entry.getValue().serialize());
            return tag;
        });
    }

    @Override
    public void onBlockLinkCreate(Player player, BlockPos other) {}

    @Override
    public void onEntityLinkCreate(Player player, LivingEntity linked) {
        if (linked instanceof Player) {
            if (this.addAllowedUser((Player) linked)) {

                Component accessGrantedMessage = Component.translatable(
                        "astralsorcery.misc.link.gateway.link",
                        linked.getDisplayName())
                        .withStyle(ChatFormatting.GREEN);
                player.sendSystemMessage(accessGrantedMessage);
                linked.sendSystemMessage(accessGrantedMessage);
            }
        }
    }

    @Override
    public boolean tryLinkBlock(Player player, BlockPos other) {
        return false;
    }

    @Override
    public boolean tryLinkEntity(Player player, LivingEntity other) {
        return other instanceof Player && this.canAddAllowedUser((Player) other);
    }

    @Override
    public boolean tryUnlink(Player player, BlockPos other) {
        return false;
    }

    @Override
    public List<BlockPos> getLinkedPositions() {
        return Collections.emptyList();
    }

    @Override
    public boolean onSelect(Player player) {
        return false;
    }
}
