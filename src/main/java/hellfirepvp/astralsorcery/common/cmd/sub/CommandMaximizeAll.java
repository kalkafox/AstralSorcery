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
 * Class: CommandMaximizeAll
 * Created by HellFirePvP
 * Date: 21.07.2019 / 16:33
 */
public class CommandMaximizeAll implements Command<CommandSourceStack> {

    private static final CommandMaximizeAll CMD = new CommandMaximizeAll();

    private CommandMaximizeAll() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("maximize")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            Player target = (Player) ctx.getArgument("player", EntitySelector.class).selectOne(ctx.getSource());
                            ctx.getSource().customSuggestion(Component.literal("Success!").withStyle(ChatFormatting.GREEN), true);
                            maximizeAll(target);
                            return 0;
                        }))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        maximizeAll(context.getSource().getPlayerOrException());
        context.getSource().customSuggestion(Component.literal("Success!").withStyle(ChatFormatting.GREEN), true);
        return 0;
    }

    private static boolean maximizeAll(Player entity) {
        return ResearchManager.forceMaximizeAll(entity);
    }
}
