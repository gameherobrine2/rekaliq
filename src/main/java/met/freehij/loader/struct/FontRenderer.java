package met.freehij.loader.struct;

import met.freehij.loader.mappings.ClassMapping;
import met.freehij.loader.mappings.MethodInvoker;

@ClassMapping("net/minecraft/src/FontRenderer")
public interface FontRenderer {
	@MethodInvoker("drawStringWithShadow")
	public void drawStringWithShadow(String s, int i, int j, int k);
	
	@MethodInvoker("getStringWidth")
	public int getStringWidth(String s);
}
