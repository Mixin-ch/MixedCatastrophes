package ch.mixin.mixedCatastrophes.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandCompleter implements TabCompleter {
    private final RootCommand rootCommand;

    public CommandCompleter(RootCommand rootCommand) {
        this.rootCommand = rootCommand;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> arguments = new ArrayList<>(Arrays.asList(args));
        return rootCommand.fetchCommand(arguments).getOptions(arguments);
    }
}
