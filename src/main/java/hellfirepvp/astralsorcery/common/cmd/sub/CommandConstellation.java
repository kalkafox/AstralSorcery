/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.cmd.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import hellfirepvp.astralsorcery.common.cmd.argument.ArgumentTypeConstellation;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CommandConstellation
 * Created by HellFirePvP
 * Date: 22.11.2020 / 11:32
 */
public class CommandConstellation {

    private CommandConstellation() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("constellation")
                .requires(cs -> cs.hasPermissionLevel(2))
                .then(Commands.literal("memorize")
                        .then(Commands.argument("constellation", ArgumentTypeConstellation.any())
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> {
                                            Player target = EntityArgument.getPlayer(ctx, "player");
                                            IConstellation cst = ctx.getArgument("constellation", IConstellation.class);
                                            return markConstellationMemorized(ctx.getSource(), target, cst);
                                        }))
                                .executes(ctx -> {
                                    IConstellation cst = ctx.getArgument("constellation", IConstellation.class);
                                    return markConstellationMemorized(ctx.getSource(), null, cst);
                                })))
                .then(Commands.literal("discover")
                        .then(Commands.argument("constellation", ArgumentTypeConstellation.any())
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> {
                                            Player target = EntityArgument.getPlayer(ctx, "player");
                                            IConstellation cst = ctx.getArgument("constellation", IConstellation.class);
                                            return discoverConstellation(ctx.getSource(), target, cst);
                                        }))
                                .executes(ctx -> {
                                    IConstellation cst = ctx.getArgument("constellation", IConstellation.class);
                                    return discoverConstellation(ctx.getSource(), null, cst);
                                })));
    }

    private static int markConstellationMemorized(CommandSourceStack src, @Nullable Player target, IConstellation cst) throws CommandSyntaxException {
        Player source = src.asPlayer();
        target = target != null ? target : source;
        Component targetName = target.getDisplayName();
        PlayerProgress progress = ResearchHelper.getProgress(target, LogicalSide.SERVER);
        if (!progress.isValid() || progress.hasSeenConstellation(cst)) {
            source.sendMessage(Component.literal("Failed! ").append(targetName).append(" has already seen ").append(cst.getConstellationName())
                    .withStyle(TextFormatting.RED), Util.DUMMY_UUID);
            return 0;
        }
        if (ResearchManager.memorizeConstellation(cst, target)) {
            ResearchHelper.sendConstellationMemorizationMessage(target, progress, cst);
            source.sendMessage(Component.literal("Success! ")
                    .withStyle(TextFormatting.GREEN), Util.DUMMY_UUID);
            return Command.SINGLE_SUCCESS;
        } else {
            source.sendMessage(Component.literal("Failed!").withStyle(TextFormatting.RED), Util.DUMMY_UUID);
            return 0;
        }
    }

    private static int discoverConstellation(CommandSourceStack src, @Nullable Player target, IConstellation cst) throws CommandSyntaxException {
        Player source = src.asPlayer();
        target = target != null ? target : source;
        Component targetName = target.getDisplayName();
        PlayerProgress progress = ResearchHelper.getProgress(target, LogicalSide.SERVER);
        if (!progress.isValid() || progress.hasConstellationDiscovered(cst)) {
            source.sendMessage(Component.literal("Failed! ").append(targetName).append(" has already discovered ").append(cst.getConstellationName())
                    .withStyle(TextFormatting.RED), Util.DUMMY_UUID);
            return 0;
        }
        if (ResearchManager.discoverConstellation(cst, target)) {
            ResearchHelper.sendConstellationDiscoveryMessage(target, cst);
            source.sendMessage(Component.literal("Success! ").withStyle(TextFormatting.GREEN), Util.DUMMY_UUID);
            return Command.SINGLE_SUCCESS;
        } else {
            source.sendMessage(Component.literal("Failed!").withStyle(TextFormatting.RED), Util.DUMMY_UUID);
            return 0;
        }
    }
}
