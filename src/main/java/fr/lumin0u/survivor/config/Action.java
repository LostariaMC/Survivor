package fr.lumin0u.survivor.config;

public abstract class Action
{
	public abstract void redo();
	
	public abstract void undo();
	
	public static Action inverted(Action action)
	{
		return new Action()
		{
			@Override
			public void redo() {
				action.undo();
			}
			@Override
			public void undo() {
				action.redo();
			}
		};
	}
}
