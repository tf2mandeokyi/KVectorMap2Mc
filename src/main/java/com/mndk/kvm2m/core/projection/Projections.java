package com.mndk.kvm2m.core.projection;

import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.dymaxion.BTEDymaxionProjection;

public class Projections {

	public static final GeographicProjection BTE = new BTEDymaxionProjection();

	public static final Grs80Projection GRS80_WEST = new Grs80Projection.Grs80WestProjection();
	public static final Grs80Projection GRS80_MIDDLE = new Grs80Projection.Grs80MiddleProjection();
	public static final Grs80Projection GRS80_EAST = new Grs80Projection.Grs80EastProjection();

}
