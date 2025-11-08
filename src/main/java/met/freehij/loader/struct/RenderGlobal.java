package met.freehij.loader.struct;

import met.freehij.loader.mappings.ClassMapping;
import met.freehij.loader.mappings.MethodInvoker;

@ClassMapping("net/minecraft/src/RenderGlobal")
public interface RenderGlobal {
	@MethodInvoker("loadRenderers")
	public void loadRenderers();
}
