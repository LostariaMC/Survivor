package fr.lumin0u.survivor.mobs.mob;

import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.navigation.Navigation;
import net.minecraft.world.level.World;
import net.minecraft.world.level.pathfinder.PathEntity;

import java.util.Set;

public class SvNavigation extends Navigation
{
	public int followRange;
	
	public SvNavigation(EntityInsentient var0, World var1, int followRange)
	{
		super(var0, var1);
		this.followRange = followRange;
	}
	
	public void setFollowRange(int followRange)
	{
		this.followRange = followRange;
	}
	
	@Override
	protected PathEntity a(Set<BlockPosition> var0, int var1, boolean var2, int var3) {
		return this.a(var0, var1, var2, var3, (float) followRange);// 174
	}
	/*
	
	public SvNavigation(EntityInsentient entityInsentient, World world)
	{
		super(entityInsentient, world);
		AttributeInstance attribute = null;
		attribute = new AttributeModifiable(null, GenericAttributes.FOLLOW_RANGE)
		{
			@Override
			public double getValue()
			{
				return followRange;
			}
		};
		try
		{
			Field a = NavigationAbstract.class.getDeclaredField("a");
			a.setAccessible(true);
			a.set(this, attribute);
		} catch(ReflectiveOperationException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setFollowRange(double followRange)
	{
		this.followRange = followRange;
	}
	
	@Override
	public void k()
	{
		try
		{
			Field f = NavigationAbstract.class.getDeclaredField("f");
			f.setAccessible(true);
			f.set(this, (int) f.get(this) + 1);
			//++this.f;
		} catch(ReflectiveOperationException e)
		{
			e.printStackTrace();
		}
		if(!this.m())
		{// 137
			Vec3D var1;
			if(this.b())
			{// 141
				this.l();// 142
			}
			else if(this.d != null && this.d.e() < this.d.d())
			{// 144
				var1 = this.c();// 145
				Vec3D var2 = this.d.a(this.b, this.d.e());// 146
				if(var1.b > var2.b && !this.b.onGround && MathHelper.floor(var1.a) == MathHelper.floor(var2.a) && MathHelper.floor(var1.c) == MathHelper.floor(var2.c))
				{// 147
					this.d.c(this.d.e() + 1);// 148
				}
			}
			
			if(!this.m())
			{// 165
				var1 = this.d.a(this.b);// 168
				if(var1 != null)
				{// 169
					AxisAlignedBB var8 = (new AxisAlignedBB(var1.a, var1.b, var1.c, var1.a, var1.b, var1.c)).grow(0.5D, 0.5D, 0.5D);// 173
					List var3 = this.c.getCubes(this.b, var8.a(0.0D, -1.0D, 0.0D));// 174
					double var4 = -1.0D;// 175
					var8 = var8.c(0.0D, 1.0D, 0.0D);// 176
					
					AxisAlignedBB var7;
					for(Iterator var6 = var3.iterator(); var6.hasNext(); var4 = var7.b(var8, var4))
					{// 177 178
						var7 = (AxisAlignedBB) var6.next();
					}
					
					this.b.getControllerMove().a(var1.a, var1.b + var4, var1.c, this.e);// 181
				}
			}
		}
	}*/
}
