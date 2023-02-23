package fr.lumin0u.survivor.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class SvArgCommand {
    protected String name;
    protected String def;
    protected String use;
    protected String[] aliases;
    protected boolean hidden;
    protected int minArgs;
    protected boolean mustBePlayer;
    protected List<Player> active;
    protected int minRankPower = 60;

    public SvArgCommand(String name, String def, String use, boolean hidden, int minArgs, boolean mustBePlayer, String... aliases) {
        this.name = name;
        this.def = def;
        this.use = use;
        this.hidden = hidden;
        this.aliases = aliases;
        this.minArgs = minArgs;
        this.mustBePlayer = mustBePlayer;
    }

    public String getName() {
        return this.name;
    }

    public int getMinRankPower() {
        return this.minRankPower;
    }

    public String getDef() {
        return this.def;
    }

    public String getUse() {
        return this.use;
    }

    public int getMinArgs() {
        return this.minArgs;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean mustBeExecutedByAPlayer() {
        return this.mustBePlayer;
    }

    public List<String> getAliases() {
        return Arrays.asList(aliases);
    }

    public boolean doAliasesContainsIgnoreCase(String s) {
        Iterator var2 = this.getAliases().iterator();

        String a;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            a = (String)var2.next();
        } while(!a.equalsIgnoreCase(s));

        return true;
    }

    public List<String> getPossibleArgs(CommandSender executer, String[] args) {
        return new ArrayList<>();
    }

    public boolean isExecutableFrom(String s) {
        return this.getName().equalsIgnoreCase(s) || this.doAliasesContainsIgnoreCase(s);
    }

    public abstract void execute(CommandSender sender, String[] args);
}
