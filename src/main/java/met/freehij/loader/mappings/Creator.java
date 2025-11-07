package met.freehij.loader.mappings;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import met.freehij.loader.struct.Minecraft;

public class Creator {
	private static final HashMap<Class<?>, ProxyCreator> clz2proxyCreator = new HashMap<>();
	
	private static final ClassLoaderProxy classLoader = new ClassLoaderProxy(Creator.class.getClassLoader());
	
	static class ClassLoaderProxy extends ClassLoader{
		public ClassLoaderProxy(ClassLoader parent) {
			super(parent);
		}

		
		public Class<?> _defineClass(String name, byte[] b, int off, int len) {
			return this.defineClass(name, b, off, len);
		}
	}
	
	
	public static Method PROXY_CREATOR;
	static{
		Method[] ms = Creator.class.getMethods();
		for(Method m : ms) {
			if(m.getName().equals("proxy") && m.getParameterCount() == 2) {
				Class<?>[] pars = m.getParameterTypes();
				if(pars[0] == Object.class && pars[1] == Class.class) {
					if(PROXY_CREATOR != null) throw new RuntimeException("found more than one method that matches PROXY_CREATOR!");
					PROXY_CREATOR = m;
				}
			}
		}
		if(PROXY_CREATOR == null) throw new RuntimeException("PROXY_CREATOR not found - proxy generation wont work!");
	}
	/**
	 * Get proxy for an instance. Attempts to initialize one if proxy not found
	 * 
	 * Be careful when changing number of parameters or their types - it will most likely break generated proxies
	 */
	public static <T> T proxy(Object instance, Class<T> clz) {
		String s = proxyName(clz);
		if(instance == null) return null;
		try {
			Class clz2 = classLoader.loadClass(s);
			return (T) clz2.getConstructor(Object.class).newInstance(instance);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static ProxyCreator proxyCreator(Class<?> clz) {
		ProxyCreator s = clz2proxyCreator.get(clz);
		if(s == null) s = initProxy(clz);
		return s;
	}
	
	public static String proxyName(Class<?> clz) {
		return proxyCreator(clz).clzName;
	}
	
	private static ProxyCreator initProxy(Class<?> clz) {
		ProxyCreator px = ProxyCreator.create(clz);
		clz2proxyCreator.put(clz, px); //must be added into the array before the generation started
		byte[] arr = px.bytes();
		classLoader._defineClass(px.clzName, arr, 0, arr.length);
		return px;
	}
	
	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		Minecraft mc = proxy(Cliff.theCliff, Minecraft.class);
		mc.cliffCnt(1003);
		mc.theMinecraft().parentCliff(proxy(new Cliff(), Minecraft.class));
		mc.printStaticCliff();
	}
}
