package fr.lumin0u.survivor.utils;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;

public class Utils
{
	public static final String ARROW_W = "\u2190";
	public static final String ARROW_N = "\u2191";
	public static final String ARROW_E = "\u2192";
	public static final String ARROW_S = "\u2193";
	public static final String ARROW_NW = "\u2196";
	public static final String ARROW_NE = "\u2197";
	public static final String ARROW_SE = "\u2198";
	public static final String ARROW_SW = "\u2199";
	
	
	public static boolean startsLikely(String uncertain, String toFind)
	{
		return uncertain.length() <= toFind.length() && uncertain.equalsIgnoreCase(toFind.substring(0, uncertain.length()));
	}
	
	public static double square(double d)
	{
		return d * d;
	}
	
	public static <T> List<T> insert(List<T> list, T element, int index)
	{
		if(index < 0)
		{
			index = 0;
		}
		else if(index > list.size())
		{
			index = list.size();
		}
		
		if(!list.isEmpty())
		{
			T last = list.get(list.size() - 1);
			list.add(last);
			
			for(int i = list.size() - 2; i >= index; --i)
			{
				list.set(i + 1, list.get(i));
			}
			
			list.set(index, element);
		}
		else
		{
			list.add(element);
		}
		
		return list;
	}
	
	public static <T> List<T> remove(List<T> list, T object, int removes)
	{
		for(int j = 0; j < removes; ++j)
		{
			for(int i = 0; i < list.size(); ++i)
			{
				if(list.get(i).equals(object))
				{
					list.remove(i);
					break;
				}
			}
		}
		
		return list;
	}
	
	public static int occurencesOf(String searched, String container)
	{
		int occ = container.equals(searched) ? 1 : 0;
		
		for(int i = 0; i < container.length() - searched.length(); ++i)
		{
			boolean ok = true;
			
			for(int k = 0; k < searched.length(); ++k)
			{
				if(searched.charAt(k) != container.charAt(i + k))
				{
					ok = false;
				}
			}
			
			if(ok)
			{
				++occ;
			}
		}
		
		return occ;
	}
	
	public static double round(double nb, int afterComma)
	{
		return Math.floor(nb * Math.pow(10.0D, (double) afterComma)) / Math.pow(10.0D, (double) afterComma);
	}
	
	public static double clamp(double a, double b, double c)
	{
		return Math.max(b, Math.min(a, c));
	}
	
	public static int clamp(int a, int b, int c)
	{
		return Math.max(b, Math.min(a, c));
	}
	
	public static <T, R> R ifNotNullApply(T obj, Function<T, R> function)
	{
		return ifNotNullApply(obj, function, (Supplier<R>) () -> null);
	}
	
	public static <T, R> R ifNotNullApply(T obj, Function<T, R> function, R ifNull)
	{
		return ifNotNullApply(obj, function, (Supplier<R>) () -> ifNull);
	}
	
	public static <T, R> R ifNotNullApply(T obj, Function<T, R> function, Supplier<R> ifNull)
	{
		return obj == null ? ifNull.get() : function.apply(obj);
	}
	
	public static <T> void ifNotNullAccept(T obj, Consumer<T> function)
	{
		if(obj != null)
			function.accept(obj);
	}
	
	public static String title(String s) {
		if(s.isEmpty())
			return s;
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}
	
	public static <T> Collector<T, ?, Optional<T>> randomCollector() {
		return randomCollector(new Random());
	}
	
	public static <T> Collector<T, ?, Optional<T>> randomCollector(Random random) {
		final class IntTPair<E>
		{
			private int n;
			private E obj;
			
			IntTPair(int n, E obj) {
				this.n = n;
				this.obj = obj;
			}
		}
		
		return new Collector<T, IntTPair<T>, Optional<T>>() {
			@Override
			public Supplier<IntTPair<T>> supplier() {
				return () -> new IntTPair<>(0, null);
			}
			
			@Override
			public BiConsumer<IntTPair<T>, T> accumulator() {
				return (acc, obj) -> {
					acc.n++;
					if(random.nextInt(acc.n) == 0)
						acc.obj = obj;
				};
			}
			
			@Override
			public BinaryOperator<IntTPair<T>> combiner() {
				return (l, r) -> new IntTPair<>(l.n + r.n, random.nextInt(l.n + r.n) < l.n ? l.obj : r.obj);
			}
			
			@Override
			public Function<IntTPair<T>, Optional<T>> finisher() {
				return p -> Optional.ofNullable(p.obj);
			}
			
			@Override
			public Set<Characteristics> characteristics() {
				return Set.of(Characteristics.CONCURRENT, Characteristics.UNORDERED);
			}
		};
	}
}
