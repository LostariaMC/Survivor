package fr.lumin0u.survivor.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class RDPicker
{
	public static <T> Collector<T, LinkedList<T>, T> collector()
	{
		return collector(new Random());
	}
	
	public static <T> Collector<T, LinkedList<T>, T> collector(Random random)
	{
		return new Collector<T, LinkedList<T>, T>() {
			@Override
			public Supplier<LinkedList<T>> supplier() {
				return LinkedList::new;
			}
			
			@Override
			public BiConsumer<LinkedList<T>, T> accumulator() {
				return LinkedList::add;
			}
			
			@Override
			public BinaryOperator<LinkedList<T>> combiner() {
				return (l, r) -> {l.addAll(r); return l;};
			}
			
			@Override
			public Function<LinkedList<T>, T> finisher() {
				return l -> l.get(random.nextInt(l.size()));
			}
			
			@Override
			public Set<Characteristics> characteristics() {
				return Set.of();
			}
		};
	}
	
	public static <T> T collect(Stream<? extends T> s) {
		return collect(s, new Random());
	}
	
	public static <T> T collect(Stream<? extends T> s, Random random) {
		return s.collect(collector(random));
	}
	
	public static <T> T choice(Collection<? extends T> l) {
		return choice(l, new Random());
	}
	
	public static <T> T choice(Collection<? extends T> l, Random random) {
		return l.stream().collect(collector(random));
	}
}
