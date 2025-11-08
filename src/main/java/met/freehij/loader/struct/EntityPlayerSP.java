package met.freehij.loader.struct;

import met.freehij.loader.mappings.ClassMapping;
import met.freehij.loader.mappings.FieldMapping;

@ClassMapping("net/minecraft/src/EntityPlayerSP")
public interface EntityPlayerSP extends EntityLiving{
	@FieldMapping("fallDistance")
	public float fallDistance();
	
	@FieldMapping("fallDistance")
	public float fallDistance(float f);
	
	@FieldMapping("stepHeight")
	public float stepHeight(float f);
}
