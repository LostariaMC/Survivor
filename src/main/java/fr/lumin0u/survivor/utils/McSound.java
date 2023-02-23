package fr.lumin0u.survivor.utils;

import org.bukkit.Sound;

public class McSound
{
	private String sound;
	private float pitch;
	private float volume;
	
	public McSound()
	{
		this("", 1, 0);
	}
	
	public McSound(String sound, float pitch, float volume)
	{
		this.sound = sound;
		this.pitch = pitch;
		this.volume = volume;
	}
	
	public McSound(String sound, float volume)
	{
		this(sound, 1, volume);
	}
	
	public McSound(Sound sound, float volume)
	{
		this(sound, 1, volume);
	}
	
	public McSound(Sound sound, float pitch, float volume)
	{
		this(sound.getKey().getKey(), pitch, volume);
	}
	
	public float getPitch()
	{
		return pitch;
	}
	
	public String getSound()
	{
		return sound;
	}
	
	public float getVolume()
	{
		return volume;
	}
}
