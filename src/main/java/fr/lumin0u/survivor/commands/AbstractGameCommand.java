package fr.lumin0u.survivor.commands;

public abstract class AbstractGameCommand extends SvArgCommand
{
	public AbstractGameCommand(String name, String def, String use, boolean hidden, int minArgs, boolean mustBePlayer, String... aliases) {
		super(name, def, use, hidden, minArgs, mustBePlayer, aliases);
	}
}
