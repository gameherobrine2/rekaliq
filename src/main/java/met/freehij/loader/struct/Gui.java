package met.freehij.loader.struct;

import met.freehij.loader.mappings.ClassMapping;
import met.freehij.loader.mappings.MethodInvoker;
import met.freehij.loader.mappings.AccessUtils;

@ClassMapping("net/minecraft/src/Gui")
public interface Gui extends AccessUtils{
	@MethodInvoker("drawRect")
	public void drawRect(int i, int j, int k, int l, int i1);
}
