package met.freehij.loader.struct;

import met.freehij.loader.mappings.ClassMapping;
import met.freehij.loader.mappings.FieldMapping;

@ClassMapping("net/minecraft/src/PlayerControllerMP")
public interface PlayerControllerMP {
	@FieldMapping("curBlockDamageMP")
	public float curBlockDamageMP(float f);
}
