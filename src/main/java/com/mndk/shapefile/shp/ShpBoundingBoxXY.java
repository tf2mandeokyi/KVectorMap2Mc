package com.mndk.shapefile.shp;

import java.io.IOException;
import java.io.InputStream;

import com.mndk.shapefile.util.Endian;

public class ShpBoundingBoxXY {

	public final double xmin, ymin, xmax, ymax;
	
	public ShpBoundingBoxXY(InputStream is) throws IOException {
		this.xmin = Endian.readDoubleLittle(is); this.ymin = Endian.readDoubleLittle(is);
		this.xmax = Endian.readDoubleLittle(is); this.ymax = Endian.readDoubleLittle(is);
	}
	
	public ShpBoundingBoxXY(double xmin, double ymin, double xmax, double ymax) {
		this.xmin = xmin; this.ymin = ymin;
		this.xmax = xmax; this.ymax = ymax;
	}
	
	@Override
	public String toString() {
		return "x: [" + xmin + ", " + xmax + "], y: [" + ymin + ", " + ymax + "]";
	}
	
}
