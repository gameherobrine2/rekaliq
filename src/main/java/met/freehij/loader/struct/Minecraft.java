package met.freehij.loader.struct;

import met.freehij.loader.mappings.ClassMapping;
import met.freehij.loader.mappings.FieldMapping;
import met.freehij.loader.mappings.MethodInvoker;
import met.freehij.loader.mappings.RawAccess;
import met.freehij.loader.mappings.StaticFieldMapping;
import met.freehij.loader.mappings.StaticMethodInvoker;

@ClassMapping("met/freehij/loader/mappings/Cliff")
public interface Minecraft extends RawAccess{
	@StaticFieldMapping("theCliff")
	public Minecraft theMinecraft();
	
	@StaticFieldMapping("theCliff")
	public Minecraft theMinecraft(Minecraft mc);
	
	@StaticFieldMapping("cliffstatic")
	public short cliffStatic();
	
	@StaticFieldMapping("cliffstatic")
	public short cliffStatic(short i);
	
	@FieldMapping("cliffCnt")
	public int cliffCnt();
	
	@FieldMapping("cliffCnt")
	public int cliffCnt(int i);
	
	@FieldMapping("parentCliff")
	public Minecraft parentCliff();

	@FieldMapping("parentCliff")
	public Minecraft parentCliff(Minecraft mc);
	
	@MethodInvoker("printCliff")
	public void printCliff();
	
	@StaticMethodInvoker("printStaticCliff")
	public void printStaticCliff();
}
