package met.freehij.loader.struct;

import met.freehij.loader.mappings.ClassMapping;
import met.freehij.loader.mappings.MethodInvoker;

@ClassMapping("net/minecraft/src/EntityLiving")
public interface EntityLiving extends Entity{

	@MethodInvoker("jump")
	public void jump();
}
