package met.freehij.loader.struct;

import met.freehij.loader.mappings.ClassMapping;
import met.freehij.loader.mappings.FieldMapping;

@ClassMapping("net/minecraft/src/GuiButton")
public interface GuiButton extends Gui{
	
	@FieldMapping("xPosition")
    public int xPosition();
	@FieldMapping("yPosition")
    public int yPosition();
	
	@FieldMapping("width")
    public int width();
	@FieldMapping("height")
    public int height();
	
	@FieldMapping("id")
	public int id();
	
	@FieldMapping("displayString")
	public String displayString(String s);
}
