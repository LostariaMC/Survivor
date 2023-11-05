package fr.lumin0u.survivor.config;

import java.util.List;

public abstract class Action
{
	public abstract void redo();
	
	public abstract void undo();
	
	public static Action inverted(Action action)
	{
		return Action.of(action::undo, action::redo);
	}
	
	public static Action of(Runnable redo, Runnable undo) {
		return new Action() {
			@Override
			public void redo() {
				redo.run();
			}
			
			@Override
			public void undo() {
				undo.run();
			}
		};
	}
	
	public static <T> Action ofAdd(List<T> list, T obj) {
		return new Action() {
			@Override
			public void redo() {
				list.add(obj);
			}
			
			@Override
			public void undo() {
				list.remove(obj);
			}
		};
	}
}
