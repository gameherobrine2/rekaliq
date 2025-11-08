package met.freehij.loader.struct;

import met.freehij.loader.mappings.ClassMapping;
import met.freehij.loader.mappings.Creator;
import met.freehij.loader.mappings.FieldMapping;
import met.freehij.loader.mappings.AccessUtils;
import met.freehij.loader.mappings.StaticMethodInvoker;

@ClassMapping("net/minecraft/src/AxisAlignedBB")
public interface AxisAlignedBB extends AccessUtils{
	
	public static AxisAlignedBB nul() {
		return Creator.proxy(null, AxisAlignedBB.class, false);
	}
	
	@FieldMapping("minX")
	public double minX();
	@FieldMapping("minY")
	public double minY();
	@FieldMapping("minZ")
	public double minZ();
	
	@FieldMapping("maxX")
	public double maxX();
	@FieldMapping("maxY")
	public double maxY();
	@FieldMapping("maxZ")
	public double maxZ();

	@StaticMethodInvoker("getBoundingBoxFromPool")
	public AxisAlignedBB getBoundingBoxFromPool(double x, double y, double z, double x1, double y2, double z2);
}
