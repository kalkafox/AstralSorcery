/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.cmd.sub;

import net.minecraft.network.chat.Component;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.ChatFormatting;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CommandExp
 * Created by HellFirePvP
 * Date: 21.07.2019 / 20:19
 */
public class CommandExp implements Command<CommandSourceStack> {

    private static final CommandExp CMD = new CommandExp();

    private CommandExp() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("exp")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("exp", LongArgumentType.longArg())
                                .executes(CMD)));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = (Player) context.getArgument("player", EntitySelector.class).findSinglePlayer(context.getSource());
        long futureXp = LongArgumentType.getLong(context, "exp");

        if (ResearchManager.setExp(player, futureXp)) {
            context.getSource().sendSuccess(() -> 
                    Component.literal("Success! Player exp has been set to " + futureXp).withStyle(ChatFormatting.GREEN), true);
        } else {
            context.getSource().sendSuccess(() -> 
                    Component.literal("Failed! Player specified doesn't seem to have a research progress!").withStyle(ChatFormatting.RED), true);
        }
        return 0;
    }
}
