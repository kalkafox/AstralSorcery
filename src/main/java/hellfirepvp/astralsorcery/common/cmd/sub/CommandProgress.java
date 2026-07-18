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
import hellfirepvp.astralsorcery.common.data.research.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.server.command.EnumArgument;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CommandProgress
 * Created by HellFirePvP
 * Date: 22.11.2020 / 13:23
 */
public class CommandProgress {

    private CommandProgress() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("progress")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        /*.then(Commands.literal("next")
                                .executes(ctx -> {
                                    Player src = ctx.getSource().asPlayer();
                                    Player target = EntityArgument.getPlayer(ctx, "player");
                                    PlayerProgress prog = ResearchHelper.getProgress(target, LogicalSide.SERVER);
                                    ProgressionTier next = prog.getTierReached().next();
                                    return pushPlayerToProgress(src, target, next);
                                }))*/
                        .then(Commands.argument("progress", EnumArgument.enumArgument(ProgressionTier.class))
                                .executes(ctx -> {
                                    Player src = ctx.getSource().getPlayerOrException();
                                    Player target = EntityArgument.getPlayer(ctx, "player");
                                    ProgressionTier goal = ctx.getArgument("progress", ProgressionTier.class);
                                    return pushPlayerToProgress(src, target, goal);
                                })));
    }

    private static int pushPlayerToProgress(CommandSource src, Player target, ProgressionTier goal) {
        Component targetName = target.getDisplayName();
        PlayerProgress progress = ResearchHelper.getProgress(target, LogicalSide.SERVER);
        if (!progress.isValid() || progress.getTierReached().isThisLaterOrEqual(goal)) {
            src.sendSystemMessage(Component.literal("Failed! ").append(targetName).append("'s progress is higher or equal to ").append(goal.name())
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        ResearchProgression research = null;
        switch (goal) {
            case DISCOVERY:
                research = ResearchProgression.DISCOVERY;
                break;
            case BASIC_CRAFT:
                research = ResearchProgression.BASIC_CRAFT;
                break;
            case ATTUNEMENT:
                research = ResearchProgression.ATTUNEMENT;
                break;
            case CONSTELLATION_CRAFT:
                research = ResearchProgression.CONSTELLATION;
                break;
            case TRAIT_CRAFT:
                research = ResearchProgression.RADIANCE;
                break;
            case BRILLIANCE:
                research = ResearchProgression.BRILLIANCE;
                break;
            default:
                break;
        }
        if (research == null) {
            src.sendSystemMessage(Component.literal("Invalid progression tier: " + goal.name()).withStyle(ChatFormatting.RED));
        }
        if (ResearchManager.grantProgress(target, goal) && ResearchManager.grantResearch(target, research)) {
            src.sendSystemMessage(Component.literal("Success!").withStyle(ChatFormatting.GREEN));
            return Command.SINGLE_SUCCESS;
        } else {
            src.sendSystemMessage(Component.literal("Failed!").withStyle(ChatFormatting.RED));
            return 0;
        }
    }
}
