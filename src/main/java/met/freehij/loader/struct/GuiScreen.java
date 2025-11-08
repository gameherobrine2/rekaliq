package met.freehij.loader.struct;

import java.util.List;

import met.freehij.loader.mappings.AccessUtils;
import met.freehij.loader.mappings.ClassMapping;
import met.freehij.loader.mappings.Creator;
import met.freehij.loader.mappings.FieldMapping;
import met.freehij.loader.mappings.StaticMethodInvoker;

@ClassMapping("net/minecraft/src/GuiScreen")
public interface GuiScreen extends AccessUtils{
	@FieldMapping("controlList")
	public List controlList();
	
	@FieldMapping("width")
	public int width();
	
	@FieldMapping("height")
	public int height();

	@StaticMethodInvoker("getClipboardString")
	public String _getClipboardString();
	
	public static String getClipboardString() {
		return Creator.proxy(null, GuiScreen.class, false)._getClipboardString();
	}
}
