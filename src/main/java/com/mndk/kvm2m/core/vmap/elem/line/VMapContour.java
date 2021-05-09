package com.mndk.kvm2m.core.vmap.elem.line;

import com.mndk.kvm2m.core.util.EdgeGenerator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector.VMapElementStyle;
import com.mndk.kvm2m.core.vmap.elem.VMapElementLayer;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.world.World;

public class VMapContour extends VMapPolyline {

	public final int elevation;
	
	public VMapContour(VMapElementLayer parent, Vector2DH[] vertexes, Object[] rowData) {
		super(parent, new Vector2DH[][] {vertexes}, rowData, false);
		this.elevation = VMapElementStyleSelector.getStyle(this)[0].y;
	}
	
	protected void generateOutline(FlatRegion region, World w, TriangleList triangleList) {
		
		VMapElementStyle[] styles = VMapElementStyleSelector.getStyle(this);
		if(styles == null) return;
		for(VMapElementStyle style : styles) {
			if(style == null) continue; if(style.state == null) continue;
			
			EdgeGenerator lineGenerator = new EdgeGenerator.TerrainLine(
					(x, z) -> this.elevation, 
					w, region, style.state
			);
			
			for(int j = 0; j < vertexList.length; ++j) {
				Vector2DH[] temp = vertexList[j];
				for(int i = 0; i < temp.length - 1; ++i) {
					lineGenerator.generate(temp[i], temp[i+1]);
				}
				if(this.isClosed()) {
					lineGenerator.generate(temp[temp.length-1], temp[0]);
				}
			}
		}
	}
	
}
