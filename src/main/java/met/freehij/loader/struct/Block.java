package met.freehij.loader.struct;

import met.freehij.loader.mappings.ClassMapping;
import met.freehij.loader.mappings.FieldMapping;

@ClassMapping("net/minecraft/src/Block")
public interface Block {
	@FieldMapping("blockID")
	public int blockID();
	
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
}
