package met.freehij.loader.struct;

import met.freehij.loader.mappings.ClassMapping;
import met.freehij.loader.mappings.FieldMapping;
import met.freehij.loader.mappings.MethodInvoker;

@ClassMapping("net/minecraft/src/Entity")
public interface Entity {
	@MethodInvoker("isInWater")
	public boolean isInWater();

	@FieldMapping("motionX")
	public double motionX();
	
	@FieldMapping("motionZ")
	public double motionZ();
	
	@FieldMapping("motionY")
	public double motionY();
	@FieldMapping("motionY")
	public double motionY(double d);
	
	@FieldMapping("onGround")
	public boolean onGround(boolean b);
	
	@FieldMapping("posX")
	public double posX();
	
	@FieldMapping("posY")
	public double posY();
	
	@FieldMapping("posZ")
	public double posZ();
	
	@FieldMapping("rotationPitch")
	public float rotationPitch(float f);
	
	@FieldMapping("rotationYaw")
	public float rotationYaw(float f);
	
	@MethodInvoker("setPosition")
	public void setPosition(double x, double y, double z);
}
