package met.freehij.loader.struct;

import met.freehij.loader.mappings.ClassMapping;
import met.freehij.loader.mappings.MethodInvoker;

@ClassMapping("net/minecraft/src/GuiIngame")
public interface GuiIngame extends Gui{
	@MethodInvoker("addChatMessage")
	public void addChatMessage(String s);
}
