package fr.lumin0u.survivor.utils;

import fr.lumin0u.survivor.Survivor;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface TFSound
{
	SoundCombination SILENCE = new SoundCombination();
	
	TFSound PLAYER_FALL = new SimpleSound(Sound.BLOCK_ANVIL_BREAK, 10000.0f, 1f, SoundCategory.PLAYERS);
	TFSound PLAYER_DEATH = new SimpleSound(Sound.ENTITY_WITHER_HURT, 10000.0f, 0.5f, 0.6f, SoundCategory.PLAYERS);
	TFSound MELEE_MISS = new SimpleSound(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.7f, SoundCategory.PLAYERS);
	
	TFSound GUN_SHOT = new SimpleSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1.2f, SoundCategory.PLAYERS);
	TFSound SHOTGUN_SHOT = new SimpleSound(Sound.ENTITY_GENERIC_EXPLODE, 1f, 2f, SoundCategory.PLAYERS);
	TFSound RAILGUN_SHOT = new SimpleSound(Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1f, 2f, SoundCategory.PLAYERS);
	TFSound FREEZER_SHOT = new SimpleSound(Sound.ENTITY_WITHER_SHOOT, 1f, 1.5f, SoundCategory.PLAYERS);
	TFSound FLAME_THROWER_SHOT = new SimpleSound(Sound.ENTITY_BLAZE_SHOOT, 1f, 1.2f, SoundCategory.PLAYERS);
	TFSound EXPLOSION = new SimpleSound(Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f, SoundCategory.PLAYERS);
	
	TFSound AMMO_TAKE = new SimpleSound(Sound.BLOCK_CHAIN_BREAK, 1f, 1.2f, SoundCategory.PLAYERS);
	TFSound RELOAD = new SimpleSound(Sound.BLOCK_IRON_DOOR_OPEN, 1f, 1.5f, SoundCategory.PLAYERS);
	
	TFSound ZOMBIE_ATTACK_FENCE = new SimpleSound(Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 0.2f, 1f, SoundCategory.PLAYERS);
	TFSound ZOMBIE_HURT = new SimpleSound(Sound.ENTITY_ZOMBIE_HURT, 0.5f, 0.8f, 1f, SoundCategory.PLAYERS);
	TFSound ZOMBIE_DEATH = new SimpleSound(Sound.ENTITY_ZOMBIE_DEATH, 0.5f, 0.8f, 1f, SoundCategory.PLAYERS);
	TFSound BABY_ZOMBIE_HURT = new SimpleSound(Sound.ENTITY_ZOMBIE_HURT, 0.5f, 1.3f, 1.5f, SoundCategory.PLAYERS);
	TFSound BABY_ZOMBIE_DEATH = new SimpleSound(Sound.ENTITY_ZOMBIE_DEATH, 0.5f, 1.3f, 1.5f, SoundCategory.PLAYERS);
	
	public void playTo(WrappedPlayer player);
	public void play(Location location);
	public void play(Location location, List<WrappedPlayer> listeners);
	public boolean isSilence();
	public TFSound withVolume(float volume);
	
	public static SimpleSound simple(Sound sound) {
		return new SimpleSound(sound, 1f, 1f, SoundCategory.MASTER);
	}
	
	public static class SimpleSound implements TFSound
	{
		private final Sound sound;
		private final float volume;
		private final float pitchMin;
		private final float pitchMax;
		private final SoundCategory category;
		
		public SimpleSound(Sound sound, float volume, float pitchMin, float pitchMax, SoundCategory category) {
			this.sound = sound;
			this.volume = volume;
			this.pitchMin = pitchMin;
			this.pitchMax = pitchMax;
			this.category = category;
		}
		
		public SimpleSound(Sound sound, float volume, float pitch, SoundCategory category) {
			this(sound, volume, pitch, pitch, category);
		}
		
		private float getPitch() {
			return pitchMin == pitchMax ? pitchMin : new Random().nextFloat(pitchMin, pitchMax);
		}
		
		@Override
		public void playTo(WrappedPlayer player) {
			if(isSilence())
				return;
			if(player.isOnline()) {
				if(category == null)
					player.toBukkit().playSound(player.toBukkit().getLocation(), sound, volume, getPitch());
				else
					player.toBukkit().playSound(player.toBukkit().getLocation(), sound, category, volume, getPitch());
			}
		}
		
		@Override
		public void play(Location location) {
			if(isSilence())
				return;
			
			if(category == null)
				location.getWorld().playSound(location, sound, volume, getPitch());
			else
				location.getWorld().playSound(location, sound, category, volume, getPitch());
		}
		
		@Override
		public void play(Location location, List<WrappedPlayer> listeners) {
			if(isSilence())
				return;
			
			listeners.stream().filter(WrappedPlayer::isOnline).map(WrappedPlayer::toBukkit).forEach(player -> {
				if(category == null)
					player.playSound(location, sound, volume, getPitch());
				else
					player.playSound(location, sound, category, volume, getPitch());
			});
		}
		
		@Override
		public boolean isSilence() {
			return sound == null;
		}
		
		public Sound sound() {return sound;}
		
		public float volume() {return volume;}
		
		public float pitchMin() {return pitchMin;}
		
		public float pitchMax() {return pitchMax;}
		
		public SoundCategory category() {return category;}
		
		@Override
		public TFSound withVolume(float volume) {
			return new SimpleSound(sound, volume, pitchMin, pitchMax, category);
		}
	}
	
	public static class InstrumentNote implements TFSound
	{
		private final Instrument instrument;
		private final Note note;
		
		public InstrumentNote(Instrument instrument, Note note) {
			this.instrument = instrument;
			this.note = note;
		}
		
		@Override
		public void playTo(WrappedPlayer player) {
			player.toBukkit().playNote(player.toBukkit().getLocation(), instrument, note);
		}
		
		@Override
		public void play(Location location) {
			location.getNearbyEntitiesByType(Player.class, 32).forEach(p -> {
				p.playNote(location, instrument, note);
			});
		}
		
		@Override
		public void play(Location location, List<WrappedPlayer> listeners) {
			location.getNearbyEntitiesByType(Player.class, 32).stream().map(WrappedPlayer::of).filter(listeners::contains).forEach(p -> {
				p.toBukkit().playNote(location, instrument, note);
			});
		}
		
		@Override
		public boolean isSilence() {
			return false;
		}
		
		@Override
		public TFSound withVolume(float volume) {
			return this;
		}
	}
	
	public static class SoundCombination implements TFSound
	{
		private final Map<TFSound, Integer> soundDelays;
		
		public SoundCombination(TFSound... sounds) {
			this(Arrays.stream(sounds).collect(Collectors.toMap(Function.identity(), s -> 0)));
		}
		
		public SoundCombination(Collection<TFSound> sounds) {
			this(sounds.stream().collect(Collectors.toMap(Function.identity(), s -> 0)));
		}
		
		public SoundCombination(Map<TFSound, Integer> soundDelays) {
			this.soundDelays = Collections.unmodifiableMap(soundDelays);
		}
		
		@Override
		public void playTo(WrappedPlayer player) {
			soundDelays.forEach((sound, delay) -> {
				if(delay == 0) {
					sound.playTo(player);
				}
				else {
					Bukkit.getScheduler().runTaskLater(Survivor.getInstance(), () -> sound.playTo(player), delay);
				}
			});
		}
		
		@Override
		public void play(Location location) {
			Location realLoc = location.clone();
			soundDelays.forEach((sound, delay) -> {
				if(delay == 0) {
					sound.play(realLoc);
				}
				else {
					Bukkit.getScheduler().runTaskLater(Survivor.getInstance(), () -> sound.play(realLoc), delay);
				}
			});
		}
		
		@Override
		public void play(Location location, List<WrappedPlayer> listeners) {
			Location realLoc = location.clone();
			soundDelays.forEach((sound, delay) -> {
				if(delay == 0) {
					sound.play(realLoc, listeners);
				}
				else {
					Bukkit.getScheduler().runTaskLater(Survivor.getInstance(), () -> sound.play(realLoc, listeners), delay);
				}
			});
		}
		
		@Override
		public boolean isSilence() {
			return soundDelays.keySet().stream().allMatch(TFSound::isSilence);
		}
		
		@Override
		public TFSound withVolume(float volume) {
			throw new UnsupportedOperationException("j'ai eu la flemme de le coder");
		}
	}
}
