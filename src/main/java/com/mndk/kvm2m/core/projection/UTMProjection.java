package com.mndk.kvm2m.core.projection;

public class UTMProjection extends Proj4jProjection {

	public UTMProjection() {
		super("EPSG:5186", new String[] {
				"+proj=tmerc", "+lat_0=38", "+lon_0=127", "+k=1", "+x_0=200000", "+y_0=600000", "+ellps=GRS80", "+units=m", "+no_defs"
		});
	}

}
