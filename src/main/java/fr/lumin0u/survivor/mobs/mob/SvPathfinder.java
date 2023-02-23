package fr.lumin0u.survivor.mobs.mob;

import net.minecraft.world.level.pathfinder.PathfinderAbstract;

public abstract class SvPathfinder extends PathfinderAbstract
{
	/*protected float oldWaterCost;
	private final Long2ObjectMap<PathType> pathTypesByPosCache = new Long2ObjectOpenHashMap();
	private final Object2BooleanMap<AxisAlignedBB> collisionCache = new Object2BooleanOpenHashMap();
	
	public SvPathfinder() {
	}// 41
	
	
	@Override
	public void a(ChunkCache var0, EntityInsentient var1) {
		prepare(var0, var1);
	}
	
	public void prepare(ChunkCache chunkcache, EntityInsentient entityinsentient) {
		super.a(chunkcache, entityinsentient);
		this.oldWaterCost = entityinsentient.a(PathType.j);
	}
	
	@Override
	public void a() {
		done();
	}
	
	public void done() {
		this.b.a(PathType.j, this.oldWaterCost);
		this.pathTypesByPosCache.clear();
		this.collisionCache.clear();
		super.a();
	}
	
	@Override
	public PathPoint b() // getStart
	{
		MutableBlockPosition mutableBlockPosition = new MutableBlockPosition();// 60
		int i = this.b.dd();// 61
		IBlockData iblockdata = this.a.a_(mutableBlockPosition.c(this.b.dc(), (double)i, this.b.di()));// 62
		BlockPosition blockposition;
		if (!this.b.a(iblockdata.o())) {// 64
			if (this.f() && this.b.aQ()) {// 70
				while(true) {
					if (!iblockdata.a(Blocks.A) && iblockdata.o() != FluidTypes.c.a(false)) {// 71
						--i;// 75
						break;
					}
					
					++i;// 72
					iblockdata = this.a.a_(mutableBlockPosition.c(this.b.dc(), (double)i, this.b.di()));// 73
				}
			} else if (this.b.aw()) {// 77
				i = MathHelper.b(this.b.de() + 0.5D);// 78
			} else {
				for(blockposition = this.b.cW(); (this.a.a_(blockposition).g() || this.a.a_(blockposition).a(this.a, blockposition, PathMode.a)) && blockposition.v() > this.b.s.u_(); blockposition = blockposition.c()) {// 80 81 82
				}
				
				i = blockposition.b().v();// 84
			}
		} else {
			while(true) {
				if (!this.b.a(iblockdata.o())) {// 65
					--i;// 69
					break;
				}
				
				++i;// 66
				iblockdata = this.a.a_(mutableBlockPosition.c(this.b.dc(), (double)i, this.b.di()));// 67
			}
		}
		
		blockposition = this.b.cW();// 88
		PathType pathtype = this.a(this.b, blockposition.u(), i, blockposition.w());// 89
		if (this.b.a(pathtype) < 0.0F) {// 91
			AxisAlignedBB var5 = this.b.cw();// 92
			if (this.c(mutableBlockPosition.c(var5.a, (double)i, var5.c)) || this.c(mutableBlockPosition.c(var5.a, (double)i, var5.f)) || this.c(mutableBlockPosition.c(var5.d, (double)i, var5.c)) || this.c(mutableBlockPosition.c(var5.d, (double)i, var5.f))) {// 94 95 96 97
				PathPoint var6 = this.b(mutableBlockPosition);// 99
				var6.l = this.a(this.b, var6.a());// 100
				var6.k = this.b.a(var6.l);// 101
				return var6;// 102
			}
		}
		
		PathPoint pathpoint1 = this.a(blockposition.u(), i, blockposition.w());// 107
		pathpoint1.l = this.a(this.b, pathpoint1.a());// 108
		pathpoint1.k = this.b.a(pathpoint1.l);// 109
		return pathpoint1;// 110
	}
	
	private boolean c(BlockPosition var0) //hasPositiveMalus
	{
		PathType var1 = this.a(this.b, var0);// 114
		return this.b.a(var1) >= 0.0F;// 115
	}
	
	@Override
	public PathDestination a(double var0, double var2, double var4) // getGoal
	{
		return new PathDestination(this.a(MathHelper.b(var0), MathHelper.b(var2), MathHelper.b(var4)));// 120
	}
	
	@Override
	public int a(PathPoint[] apathpoint, PathPoint pathpoint) // getNeighbors
	{
		int i = 0;// 125
		int j = 0;// 126
		PathType var4 = this.a(this.b, pathpoint.a, pathpoint.b + 1, pathpoint.c);// 127
		PathType var5 = this.a(this.b, pathpoint.a, pathpoint.b, pathpoint.c);// 128
		if (this.b.a(var4) >= 0.0F && var5 != PathType.y) {// 130
			j = MathHelper.d(Math.max(1.0F, this.b.P));// 131
		}
		
		double var6 = this.a(new BlockPosition(pathpoint.a, pathpoint.b, pathpoint.c));// 134
		PathPoint var8 = this.a(pathpoint.a, pathpoint.b, pathpoint.c + 1, j, var6, EnumDirection.d, var5);// 136
		if (this.a(var8, pathpoint)) {// 137
			apathpoint[i++] = var8;// 138
		}
		
		PathPoint var9 = this.a(pathpoint.a - 1, pathpoint.b, pathpoint.c, j, var6, EnumDirection.e, var5);// 141
		if (this.a(var9, pathpoint)) {// 142
			apathpoint[i++] = var9;// 143
		}
		
		PathPoint var10 = this.a(pathpoint.a + 1, pathpoint.b, pathpoint.c, j, var6, EnumDirection.f, var5);// 146
		if (this.a(var10, pathpoint)) {// 147
			apathpoint[i++] = var10;// 148
		}
		
		PathPoint var11 = this.a(pathpoint.a, pathpoint.b, pathpoint.c - 1, j, var6, EnumDirection.c, var5);// 151
		if (this.a(var11, pathpoint)) {// 152
			apathpoint[i++] = var11;// 153
		}
		
		PathPoint var12 = this.a(pathpoint.a - 1, pathpoint.b, pathpoint.c - 1, j, var6, EnumDirection.c, var5);// 156
		if (this.a(pathpoint, var9, var11, var12)) {// 157
			apathpoint[i++] = var12;// 158
		}
		
		PathPoint var13 = this.a(pathpoint.a + 1, pathpoint.b, pathpoint.c - 1, j, var6, EnumDirection.c, var5);// 161
		if (this.a(pathpoint, var10, var11, var13)) {// 162
			apathpoint[i++] = var13;// 163
		}
		
		PathPoint var14 = this.a(pathpoint.a - 1, pathpoint.b, pathpoint.c + 1, j, var6, EnumDirection.d, var5);// 166
		if (this.a(pathpoint, var9, var8, var14)) {// 167
			apathpoint[i++] = var14;// 168
		}
		
		PathPoint var15 = this.a(pathpoint.a + 1, pathpoint.b, pathpoint.c + 1, j, var6, EnumDirection.d, var5);// 171
		if (this.a(pathpoint, var10, var8, var15)) {// 172
			apathpoint[i++] = var15;// 173
		}
		
		return i;// 176
	}
	
	protected boolean a(@Nullable PathPoint var0, PathPoint var1) //isNeighborValid
	{
		return var0 != null && !var0.i && (var0.k >= 0.0F || var1.k < 0.0F);// 180
	}
	
	protected boolean a(PathPoint var0, @Nullable PathPoint var1, @Nullable PathPoint var2, @Nullable PathPoint var3) //isDiagonalValid
	{
		if (var3 != null && var2 != null && var1 != null) {// 184
			if (var3.i) {// 188
				return false;// 189
			} else if (var2.b <= var0.b && var1.b <= var0.b) {// 192
				if (var1.l != PathType.d && var2.l != PathType.d && var3.l != PathType.d) {// 196
					boolean var4 = var2.l == PathType.h && var1.l == PathType.h && (double)this.b.cT() < 0.5D;// 202
					return var3.k >= 0.0F && (var2.b < var0.b || var2.k >= 0.0F || var4) && (var1.b < var0.b || var1.k >= 0.0F || var4);// 204
				} else {
					return false;// 198
				}
			} else {
				return false;// 193
			}
		} else {
			return false;// 185
		}
	}
	
	private boolean a(PathPoint var0)//canReachWithoutCollision
	{
		Vec3D var1 = new Vec3D((double)var0.a - this.b.dc(), (double)var0.b - this.b.de(), (double)var0.c - this.b.di());// 210 211 212 213
		AxisAlignedBB var2 = this.b.cw();// 215
		int var3 = MathHelper.e(var1.f() / var2.a());// 216
		var1 = var1.a((double)(1.0F / (float)var3));// 217
		
		for(int var4 = 1; var4 <= var3; ++var4) {// 218
			var2 = var2.c(var1);// 219
			if (this.a(var2)) {// 220
				return false;// 221
			}
		}
		
		return true;// 224
	}
	
	protected double a(BlockPosition var0) {
		return a((IBlockAccess)this.a, (BlockPosition)var0);// 228
	}
	
	public static double a(IBlockAccess var0, BlockPosition var1) {
		BlockPosition var2 = var1.c();// 232
		VoxelShape var3 = var0.a_(var2).k(var0, var2);// 233
		return (double)var2.v() + (var3.b() ? 0.0D : var3.c(EnumAxis.b));// 234
	}
	
	protected boolean c() {
		return false;// 238
	}
	
	@Nullable
	protected PathPoint a(int var0, int var1, int var2, int var3, double var4, EnumDirection var6, PathType var7) {
		PathPoint var8 = null;// 243
		MutableBlockPosition var9 = new MutableBlockPosition();// 244
		double var10 = this.a((BlockPosition)var9.d(var0, var1, var2));// 246
		if (var10 - var4 > 1.125D) {// 249
			return null;// 250
		} else {
			PathType var12 = this.a(this.b, var0, var1, var2);// 253
			float var13 = this.b.a(var12);// 255
			double var14 = (double)this.b.cT() / 2.0D;// 256
			if (var13 >= 0.0F) {// 258
				var8 = this.a(var0, var1, var2);// 259
				var8.l = var12;// 260
				var8.k = Math.max(var8.k, var13);// 261
			}
			
			if (var7 == PathType.h && var8 != null && var8.k >= 0.0F && !this.a(var8)) {// 265
				var8 = null;// 266
			}
			
			if (var12 == PathType.c || this.c() && var12 == PathType.j) {// 269
				return var8;// 270
			} else {
				if ((var8 == null || var8.k < 0.0F) && var3 > 0 && var12 != PathType.h && var12 != PathType.m && var12 != PathType.e && var12 != PathType.f) {// 273
					var8 = this.a(var0, var1 + 1, var2, var3 - 1, var4, var6, var7);// 274
					if (var8 != null && (var8.l == PathType.b || var8.l == PathType.c) && this.b.cT() < 1.0F) {// 279
						double var16 = (double)(var0 - var6.i()) + 0.5D;// 280
						double var18 = (double)(var2 - var6.k()) + 0.5D;// 281
						AxisAlignedBB var20 = new AxisAlignedBB(var16 - var14, a((IBlockAccess)this.a, (BlockPosition)var9.c(var16, (double)(var1 + 1), var18)) + 0.001D, var18 - var14, var16 + var14, (double)this.b.cU() + a((IBlockAccess)this.a, (BlockPosition)var9.c((double)var8.a, (double)var8.b, (double)var8.c)) - 0.002D, var18 + var14);// 283 285 288
						if (this.a(var20)) {// 291
							var8 = null;// 292
						}
					}
				}
				
				if (!this.c() && var12 == PathType.j && !this.f()) {// 297
					if (this.a(this.b, var0, var1 - 1, var2) != PathType.j) {// 298
						return var8;// 299
					}
					
					while(var1 > this.b.s.u_()) {// 303
						--var1;// 304
						var12 = this.a(this.b, var0, var1, var2);// 306
						if (var12 != PathType.j) {// 308
							return var8;// 313
						}
						
						var8 = this.a(var0, var1, var2);// 309
						var8.l = var12;// 310
						var8.k = Math.max(var8.k, this.b.a(var12));// 311
					}
				}
				
				if (var12 == PathType.b) {// 318
					int var16 = 0;// 321
					int var17 = var1;// 322
					
					while(var12 == PathType.b) {// 323
						--var1;// 324
						PathPoint var18;
						if (var1 < this.b.s.u_()) {// 326
							var18 = this.a(var0, var17, var2);// 327
							var18.l = PathType.a;// 328
							var18.k = -1.0F;// 329
							return var18;// 330
						}
						
						if (var16++ >= this.b.cj()) {// 333
							var18 = this.a(var0, var1, var2);// 334
							var18.l = PathType.a;// 335
							var18.k = -1.0F;// 336
							return var18;// 337
						}
						
						var12 = this.a(this.b, var0, var1, var2);// 340
						var13 = this.b.a(var12);// 341
						if (var12 != PathType.b && var13 >= 0.0F) {// 343
							var8 = this.a(var0, var1, var2);// 344
							var8.l = var12;// 345
							var8.k = Math.max(var8.k, var13);// 346
							break;// 347
						}
						
						if (var13 < 0.0F) {// 348
							var18 = this.a(var0, var1, var2);// 349
							var18.l = PathType.a;// 350
							var18.k = -1.0F;// 351
							return var18;// 352
						}
					}
				}
				
				if (var12 == PathType.h) {// 357
					var8 = this.a(var0, var1, var2);// 358
					var8.i = true;// 359
					var8.l = var12;// 360
					var8.k = var12.a();// 361
				}
				
				return var8;// 364
			}
		}
	}
	
	private boolean a(AxisAlignedBB var0) {
		return this.collisionCache.computeIfAbsent(var0, (var1) -> {// 368
			return !this.a.a(this.b, var0);
		});
	}
	
	public PathType a(IBlockAccess var0, int var1, int var2, int var3, EntityInsentient var4, int var5, int var6, int var7, boolean var8, boolean var9) {
		EnumSet<PathType> var10 = EnumSet.noneOf(PathType.class);// 374
		PathType var11 = PathType.a;// 375
		BlockPosition var12 = var4.cW();// 377
		var11 = this.a(var0, var1, var2, var3, var5, var6, var7, var8, var9, var10, var11, var12);// 379
		if (var10.contains(PathType.h)) {// 381
			return PathType.h;// 382
		} else if (var10.contains(PathType.m)) {// 385
			return PathType.m;// 386
		} else {
			PathType var13 = PathType.a;// 389
			Iterator var15 = var10.iterator();// 390
			
			while(var15.hasNext()) {
				PathType var15 = (PathType)var15.next();
				if (var4.a(var15) < 0.0F) {// 392
					return var15;// 393
				}
				
				if (var4.a(var15) >= var4.a(var13)) {// 397
					var13 = var15;// 398
				}
			}
			
			if (var11 == PathType.b && var4.a(var13) == 0.0F && var5 <= 1) {// 403
				return PathType.b;// 404
			} else {
				return var13;// 407
			}
		}
	}
	
	public PathType a(IBlockAccess var0, int var1, int var2, int var3, int var4, int var5, int var6, boolean var7, boolean var8, EnumSet<PathType> var9, PathType var10, BlockPosition var11) {
		for(int var12 = 0; var12 < var4; ++var12) {// 411
			for(int var13 = 0; var13 < var5; ++var13) {// 412
				for(int var14 = 0; var14 < var6; ++var14) {// 413
					int var15 = var12 + var1;// 414
					int var16 = var13 + var2;// 415
					int var17 = var14 + var3;// 416
					PathType var18 = this.a(var0, var15, var16, var17);// 418
					var18 = this.a(var0, var7, var8, var11, var18);// 420
					if (var12 == 0 && var13 == 0 && var14 == 0) {// 422
						var10 = var18;// 423
					}
					
					var9.add(var18);// 443
				}
			}
		}
		
		return var10;// 447
	}
	
	protected PathType a(IBlockAccess var0, boolean var1, boolean var2, BlockPosition var3, PathType var4) {
		if (var4 == PathType.u && var1 && var2) {// 451
			var4 = PathType.d;// 452
		}
		
		if (var4 == PathType.t && !var2) {// 454
			var4 = PathType.a;// 455
		}
		
		if (var4 == PathType.l && !(var0.a_(var3).b() instanceof BlockMinecartTrackAbstract) && !(var0.a_(var3.c()).b() instanceof BlockMinecartTrackAbstract)) {// 457
			var4 = PathType.m;// 458
		}
		
		if (var4 == PathType.x) {// 460
			var4 = PathType.a;// 461
		}
		
		return var4;// 463
	}
	
	private PathType a(EntityInsentient var0, BlockPosition var1) {
		return this.a(var0, var1.u(), var1.v(), var1.w());// 467
	}
	
	protected PathType a(EntityInsentient var0, int var1, int var2, int var3) {
		return (PathType)this.pathTypesByPosCache.computeIfAbsent(BlockPosition.a(var1, var2, var3), (var4) -> {// 471
			return this.a(this.a, var1, var2, var3, var0, this.d, this.e, this.f, this.e(), this.d());
		});
	}
	
	public PathType a(IBlockAccess var0, int var1, int var2, int var3) {
		return a(var0, new MutableBlockPosition(var1, var2, var3));// 476
	}
	
	public static PathType a(IBlockAccess var0, MutableBlockPosition var1) {
		int var2 = var1.u();// 487
		int var3 = var1.v();// 488
		int var4 = var1.w();// 489
		PathType var5 = b(var0, var1);// 491
		if (var5 == PathType.b && var3 >= var0.u_() + 1) {// 493
			PathType var6 = b(var0, var1.d(var2, var3 - 1, var4));// 494
			var5 = var6 != PathType.c && var6 != PathType.b && var6 != PathType.j && var6 != PathType.i ? PathType.c : PathType.b;// 495 496
			if (var6 == PathType.o) {// 498
				var5 = PathType.o;// 499
			}
			
			if (var6 == PathType.q) {// 502
				var5 = PathType.q;// 503
			}
			
			if (var6 == PathType.s) {// 506
				var5 = PathType.s;// 507
			}
			
			if (var6 == PathType.y) {// 510
				var5 = PathType.y;// 511
			}
			
			if (var6 == PathType.f) {// 514
				var5 = PathType.g;// 515
			}
		}
		
		if (var5 == PathType.c) {// 520
			var5 = a(var0, var1.d(var2, var3, var4), var5);// 521
		}
		
		return var5;// 523
	}
	
	public static PathType a(IBlockAccess var0, MutableBlockPosition var1, PathType var2) {
		int var3 = var1.u();// 527
		int var4 = var1.v();// 528
		int var5 = var1.w();// 529
		
		for(int var6 = -1; var6 <= 1; ++var6) {// 531
			for(int var7 = -1; var7 <= 1; ++var7) {// 532
				for(int var8 = -1; var8 <= 1; ++var8) {// 533
					if (var6 != 0 || var8 != 0) {// 534
						var1.d(var3 + var6, var4 + var7, var5 + var8);// 535
						IBlockData var9 = var0.a_(var1);// 536
						if (var9.a(Blocks.cN)) {// 537
							return PathType.p;// 538
						}
						
						if (var9.a(Blocks.mu)) {// 539
							return PathType.r;// 540
						}
						
						if (a(var9)) {// 541
							return PathType.n;// 542
						}
						
						if (var0.b_(var1).a(TagsFluid.a)) {// 543
							return PathType.k;// 544
						}
					}
				}
			}
		}
		
		return var2;// 550
	}
	
	protected static PathType b(IBlockAccess var0, BlockPosition var1) {
		IBlockData var2 = var0.a_(var1);// 554
		Block var3 = var2.b();// 555
		Material var4 = var2.c();// 556
		if (var2.g()) {// 558
			return PathType.b;// 559
		} else if (!var2.a(TagsBlock.I) && !var2.a(Blocks.ed) && !var2.a(Blocks.pI)) {// 562
			if (var2.a(Blocks.oO)) {// 566
				return PathType.f;// 567
			} else if (var2.a(Blocks.cN)) {// 570
				return PathType.q;// 571
			} else if (var2.a(Blocks.mu)) {// 574
				return PathType.s;// 575
			} else if (var2.a(Blocks.ns)) {// 578
				return PathType.y;// 579
			} else if (var2.a(Blocks.et)) {// 582
				return PathType.z;// 583
			} else {
				Fluid var5 = var0.b_(var1);// 586
				if (var5.a(TagsFluid.b)) {// 587
					return PathType.i;// 588
				} else if (a(var2)) {// 591
					return PathType.o;// 592
				} else if (BlockDoor.n(var2) && !(Boolean)var2.c(BlockDoor.b)) {// 595
					return PathType.u;// 596
				} else if (var3 instanceof BlockDoor && var4 == Material.K && !(Boolean)var2.c(BlockDoor.b)) {// 597
					return PathType.v;// 598
				} else if (var3 instanceof BlockDoor && (Boolean)var2.c(BlockDoor.b)) {// 599
					return PathType.t;// 600
				} else if (var3 instanceof BlockMinecartTrackAbstract) {// 603
					return PathType.l;// 604
				} else if (var3 instanceof BlockLeaves) {// 607
					return PathType.x;// 608
				} else if (var2.a(TagsBlock.L) || var2.a(TagsBlock.E) || var3 instanceof BlockFenceGate && !(Boolean)var2.c(BlockFenceGate.a)) {// 611
					return PathType.h;// 612
				} else if (!var2.a(var0, var1, PathMode.a)) {// 616
					return PathType.a;// 617
				} else {
					return var5.a(TagsFluid.a) ? PathType.j : PathType.b;// 620 621 624
				}
			}
		} else {
			return PathType.e;// 563
		}
	}
	
	public static boolean a(IBlockData var0) {
		return var0.a(TagsBlock.aw) || var0.a(Blocks.B) || var0.a(Blocks.iX) || BlockCampfire.g(var0) || var0.a(Blocks.em);// 628 629 630 631 632
	}*/
}
