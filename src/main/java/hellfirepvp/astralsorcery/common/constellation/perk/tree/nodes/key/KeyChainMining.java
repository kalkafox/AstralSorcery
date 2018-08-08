/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2018
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.perk.tree.nodes.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.perk.attribute.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.constellation.perk.attribute.type.AttributeTypeRegistry;
import hellfirepvp.astralsorcery.common.constellation.perk.tree.nodes.KeyPerk;
import hellfirepvp.astralsorcery.common.data.config.Config;
import hellfirepvp.astralsorcery.common.data.config.entry.ConfigEntry;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.tile.TileFakeTree;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.struct.BlockArray;
import hellfirepvp.astralsorcery.common.util.struct.BlockDiscoverer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyChainMining
 * Created by HellFirePvP
 * Date: 30.07.2018 / 01:09
 */
public class KeyChainMining extends KeyPerk {

    private float chainChance = 0.2F;
    private int chainLength = 4;

    public KeyChainMining(String name, int x, int y) {
        super(name, x, y);
        Config.addDynamicEntry(new ConfigEntry(ConfigEntry.Section.PERKS, name) {
            @Override
            public void loadFromConfig(Configuration cfg) {
                chainLength = cfg.getInt("ChainLength", getConfigurationSection(), chainLength, 2, 20,
                        "Defines the maximum length of a single break-chain.");
                chainChance = cfg.getFloat("ChainChance", getConfigurationSection(), chainChance, 0.01F, 1F,
                        "Defines the base chance a chain is tried to be built.");
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onBreak(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
        Side side = player.world.isRemote ? Side.CLIENT : Side.SERVER;
        PlayerProgress prog = ResearchManager.getProgress(player, side);
        if (prog != null && player instanceof EntityPlayerMP && prog.hasPerkUnlocked(this) &&
                !MiscUtils.isPlayerFakeMP((EntityPlayerMP) player) && !player.isSneaking()
                && event.getWorld() instanceof WorldServer && !player.isCreative()) {
            WorldServer world = (WorldServer) event.getWorld();
            if(doMiningChain(world, event.getPos(), event.getState(), player, side)) {
                float doubleChance = PerkAttributeHelper.getOrCreateMap(player, side)
                        .getModifier(AttributeTypeRegistry.ATTR_TYPE_MINING_CHAIN_SUCCESSIVECHAIN);
                doubleChance *= PerkAttributeHelper.getOrCreateMap(player, side).getModifier(AttributeTypeRegistry.ATTR_TYPE_INC_PERK_EFFECT);
                if (rand.nextFloat() < doubleChance) {
                    while (doMiningChain(world, event.getPos(), event.getState(), player, side)) {}
                }
            }
        }
    }

    private boolean doMiningChain(WorldServer world, BlockPos pos, IBlockState state, EntityPlayer player, Side side) {
        float ch = chainChance;
        ch = PerkAttributeHelper.getOrCreateMap(player, side)
                .modifyValue(AttributeTypeRegistry.ATTR_TYPE_MINING_CHAIN_CHANCE, ch);
        ch *= PerkAttributeHelper.getOrCreateMap(player, side).getModifier(AttributeTypeRegistry.ATTR_TYPE_INC_PERK_EFFECT);
        if (rand.nextFloat() < ch) {
            float fLength = chainLength;
            fLength = PerkAttributeHelper.getOrCreateMap(player, side)
                    .modifyValue(AttributeTypeRegistry.ATTR_TYPE_MINING_CHAIN_LENGTH, fLength);
            fLength *= PerkAttributeHelper.getOrCreateMap(player, side).getModifier(AttributeTypeRegistry.ATTR_TYPE_INC_PERK_EFFECT);
            BlockArray chain = BlockDiscoverer.discoverBlocksWithSameStateAroundChain(world, pos, state, Math.round(fLength), null,
                    ((world1, pos1, state1) ->
                            state1.getBlockHardness(world1, pos1) >= 0 &&
                                    world1.getTileEntity(pos1) == null &&
                                    !world1.isAirBlock(pos1) &&
                                    state1.getBlock().canHarvestBlock(world1, pos1, player)));
            if (!chain.isEmpty()) {
                int broken = 0;
                FakePlayer fp = AstralSorcery.proxy.getASFakePlayerServer(world);
                for (BlockPos at : chain.getPattern().keySet()) {
                    IBlockState atState = world.getBlockState(at);
                    int exp;
                    try {
                        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, atState, fp);
                        MinecraftForge.EVENT_BUS.post(event);
                        exp = event.getExpToDrop();
                        if(event.isCanceled()) return false;
                    } catch (Exception exc) {
                        return false;
                    }
                    try {
                        TileEntity te = world.getTileEntity(at);
                        Block block = atState.getBlock();
                        if(block.removedByPlayer(atState, world, at, player, true)) {
                            block.onBlockDestroyedByPlayer(world, at, atState);
                            block.harvestBlock(world, player, at, atState, te, player.getHeldItemMainhand());
                            if (exp > 0) {
                                block.dropXpOnBlockBreak(world, at, exp);
                            }
                            broken++;
                        }
                    } catch (Exception exc) {}
                }
                return broken >= chain.getPattern().size() / 2;
            }
        }
        return false;
    }

}