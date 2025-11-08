package met.freehij.loader.mappings;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ProxyCreator {
	private static final String PROXY_RAWINSTANCE_FIELDNAME = "i";
	public static abstract class ProxyParent{
		public ProxyParent(){}
		public ProxyParent(Object instance){}
	}
	private final Class<?> clz;
	private final String classMapping;
	
	private final String _clzName;
	public final String clzName;
	public ProxyCreator(Class<?> clz) {
		this.clz = clz;
		ClassMapping cm = clz.getAnnotation(ClassMapping.class);
		if(cm == null) throw new RuntimeException("Attempted to registed a class without ClassMapping("+clz+")");
		this.classMapping = cm.value();
		_clzName = "met/freehij/loader/gen/"+cm.value().replace('/', '#');
		clzName = "met.freehij.loader.gen."+cm.value().replace('/', '#');
	}
	
	private void constructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/Object;)V", null, null);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(ProxyParent.class), "<init>", "()V", false);
        
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitTypeInsn(Opcodes.CHECKCAST, classMapping);
        mv.visitFieldInsn(Opcodes.PUTFIELD, _clzName, PROXY_RAWINSTANCE_FIELDNAME, "L"+classMapping+";");
        
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
	}
	
	private int returnOpcode(Class<?> clz) {
		if(clz == void.class) return Opcodes.RETURN;
		else if(clz == int.class || clz == short.class || clz == byte.class || clz == boolean.class) return Opcodes.IRETURN;
        else if(clz == long.class) return (Opcodes.LRETURN);
        else if(clz == float.class) return Opcodes.FRETURN;
        else if(clz == double.class) return Opcodes.DRETURN;
        else {
            return Opcodes.ARETURN;
        }
	}
	
	private int loadOpcode(Type type) {
		if(type == Type.INT_TYPE || type == Type.SHORT_TYPE || type == Type.BYTE_TYPE || type == Type.BOOLEAN_TYPE) return Opcodes.ILOAD;
        else if(type == Type.LONG_TYPE) return (Opcodes.LLOAD);
        else if(type == Type.FLOAT_TYPE) return Opcodes.FLOAD;
        else if(type == Type.DOUBLE_TYPE) return Opcodes.DLOAD;
        else {
            return Opcodes.ALOAD;
        }
	}
	
	private int loadOpcode(Class<?> clz) {
		return loadOpcode(Type.getType(clz));
	}
	
	
	
	private void accessUtilsStaticClass(ClassWriter cw, Method m) {
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, m.getName(), Type.getMethodDescriptor(m), null, null);
		mv.visitCode();
		mv.visitLdcInsn(Type.getType("L"+this.classMapping+";"));
        mv.visitInsn(this.returnOpcode(m.getReturnType()));
        mv.visitMaxs(1, 1);
        mv.visitEnd();
	}
	
	private void accessUtilsThis(ClassWriter cw, Method m) {
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, m.getName(), Type.getMethodDescriptor(m), null, null);
		mv.visitCode();
		this.pushI(mv, 0, false);
        mv.visitInsn(this.returnOpcode(m.getReturnType()));
        mv.visitMaxs(1, 1);
        mv.visitEnd();
	}

	private static final Method ISINSTANCE;
	static {
		try {
			ISINSTANCE = Class.class.getMethod("isInstance", Object.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private void staticFieldSetter(ClassWriter cw, Method m) {
		if(m.getReturnType() != m.getParameterTypes()[0]) {
			throw new RuntimeException(String.format("Method return type(%s) does not match parameter 1(%s)!", m.getReturnType(), m.getParameterTypes()[0]));
		}
		
		String value = m.getAnnotation(StaticFieldMapping.class).value();
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, m.getName(), Type.getMethodDescriptor(m), null, null);
		mv.visitCode();
		Class<?> clz = m.getReturnType();

		
		if(clz.isAnnotationPresent(ClassMapping.class)) { //returns a reference to another class mapping - must access Proxy.i & create a new proxy<?>
			String v = clz.getAnnotation(ClassMapping.class).value();
			
			Creator.proxyCreator(clz).pushI(mv, 1, true);
			mv.visitFieldInsn(Opcodes.PUTSTATIC, classMapping, value, "L"+clz.getAnnotation(ClassMapping.class).value()+";");
			
			Method proxyCreator = Creator.PROXY_CREATOR;
			mv.visitFieldInsn(Opcodes.GETSTATIC, classMapping, value, "L"+v+";");
			mv.visitLdcInsn(Type.getType(clz));
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Creator.class), proxyCreator.getName(), Type.getMethodDescriptor(proxyCreator), false);
	        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(this.clz));
		}else {
			mv.visitVarInsn(loadOpcode(m.getReturnType()), 1);
			mv.visitFieldInsn(Opcodes.PUTSTATIC, classMapping, value, Type.getDescriptor(clz));
			mv.visitFieldInsn(Opcodes.GETSTATIC, classMapping, value, Type.getDescriptor(clz));
		}
        mv.visitInsn(this.returnOpcode(m.getReturnType()));
        mv.visitMaxs(1, 1);
        mv.visitEnd();
	}
	
	private void staticFieldGetter(ClassWriter cw, Method m) {
		String value = m.getAnnotation(StaticFieldMapping.class).value();
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, m.getName(), Type.getMethodDescriptor(m), null, null);
		mv.visitCode();
		
		Class<?> clz = m.getReturnType();
		if(clz.isAnnotationPresent(ClassMapping.class)) { //returns a reference to another class mapping - must create a new proxy
			String v = clz.getAnnotation(ClassMapping.class).value();
			Method proxyCreator = Creator.PROXY_CREATOR;
			mv.visitFieldInsn(Opcodes.GETSTATIC, classMapping, value, "L"+v+";");
			mv.visitLdcInsn(Type.getType(clz));
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Creator.class), proxyCreator.getName(), Type.getMethodDescriptor(proxyCreator), false);
	        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(clz));
		}else {
			mv.visitFieldInsn(Opcodes.GETSTATIC, classMapping, value, Type.getDescriptor(clz));
		}
		
        mv.visitInsn(this.returnOpcode(m.getReturnType()));
        mv.visitMaxs(1, 1);
        mv.visitEnd();
	}
	
	private void fieldGetter(ClassWriter cw, Method m) {
		String value = m.getAnnotation(FieldMapping.class).value();
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, m.getName(), Type.getMethodDescriptor(m), null, null);
		mv.visitCode();
		Class<?> clz = m.getReturnType();
		if(clz.isAnnotationPresent(ClassMapping.class)) { //returns a reference to another class mapping - must create a new proxy
			String v = clz.getAnnotation(ClassMapping.class).value();
			Method proxyCreator = Creator.PROXY_CREATOR;
			this.pushI(mv, 0, false);
			mv.visitFieldInsn(Opcodes.GETFIELD, classMapping, value, "L"+v+";");
			mv.visitLdcInsn(Type.getType(clz));
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Creator.class), proxyCreator.getName(), Type.getMethodDescriptor(proxyCreator), false);
	        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(clz));
		}else {
			this.pushI(mv, 0, false);
			mv.visitFieldInsn(Opcodes.GETFIELD, classMapping, value, Type.getDescriptor(clz));
		}
        mv.visitInsn(this.returnOpcode(m.getReturnType()));
        mv.visitMaxs(1, 1);
        mv.visitEnd();
	}
	

	private void pushI(MethodVisitor mv, int param, boolean cast) {
		mv.visitVarInsn(Opcodes.ALOAD, param);
		if(cast) mv.visitTypeInsn(Opcodes.CHECKCAST, this._clzName);
		mv.visitFieldInsn(Opcodes.GETFIELD, this._clzName, PROXY_RAWINSTANCE_FIELDNAME, "L"+this.classMapping+";");
	}
	
	private void fieldSetter(ClassWriter cw, Method m) {
		if(m.getReturnType() != m.getParameterTypes()[0]) {
			throw new RuntimeException(String.format("Method return type(%s) does not match parameter 1(%s)!", m.getReturnType(), m.getParameterTypes()[0]));
		}
		
		String value = m.getAnnotation(FieldMapping.class).value();
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, m.getName(), Type.getMethodDescriptor(m), null, null);
		mv.visitCode();
		Class<?> ret = m.getReturnType();


		this.pushI(mv, 0, false); //this.i.
		if(ret.isAnnotationPresent(ClassMapping.class)) { //returns a reference to another class mapping - must access Proxy.i & create a new proxy<?>
			String v = ret.getAnnotation(ClassMapping.class).value();
			Creator.proxyCreator(ret).pushI(mv, 1, true);
			mv.visitFieldInsn(Opcodes.PUTFIELD, classMapping, value, "L"+ret.getAnnotation(ClassMapping.class).value()+";");
			
			Method proxyCreator = Creator.PROXY_CREATOR;
			
			this.pushI(mv, 0, false); //this.i.
			mv.visitFieldInsn(Opcodes.GETFIELD, classMapping, value, "L"+v+";");
			
			mv.visitLdcInsn(Type.getType(ret));
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Creator.class), proxyCreator.getName(), Type.getMethodDescriptor(proxyCreator), false);
	        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(this.clz));
		}else {
			mv.visitVarInsn(loadOpcode(m.getReturnType()), 1); //<field>
			mv.visitFieldInsn(Opcodes.PUTFIELD, classMapping, value, Type.getDescriptor(ret)); // = 
			
			//this.i.<field>
			this.pushI(mv, 0, false);
			mv.visitFieldInsn(Opcodes.GETFIELD, classMapping, value, Type.getDescriptor(ret));
		}
        mv.visitInsn(this.returnOpcode(m.getReturnType()));
        mv.visitMaxs(1, 1);
        mv.visitEnd();
	}
	
	public void methodInvoker(ClassWriter cw, Method m) {
		//if(m.getReturnType() == void.class) {
		//	
		//}
		String value = m.getAnnotation(MethodInvoker.class).value();
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, m.getName(), Type.getMethodDescriptor(m), null, null);
		mv.visitCode();
		Class<?> ret = m.getReturnType();
		this.pushI(mv, 0, false);
		
		Type rett;
		Type params[] = new Type[m.getParameterCount()];
		Class<?> oparams[] = m.getParameterTypes();
		rett = this.clz2type(ret);
		
		int j = 1;
		for(int i = 0; i < oparams.length; ++i) {
			Class<?> p = oparams[i];
			params[i] = clz2type(p);
			j += pushType(mv, p, j);
		}
		
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.classMapping, value, Type.getMethodDescriptor(rett, params)); //TODO better method description parsing
		
        mv.visitInsn(this.returnOpcode(m.getReturnType()));
        mv.visitMaxs(1, 1);
        mv.visitEnd();
	}
	
	public void staticMethodInvoker(ClassWriter cw, Method m) {
		String value = m.getAnnotation(StaticMethodInvoker.class).value();
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, m.getName(), Type.getMethodDescriptor(m), null, null);
		mv.visitCode();
		Class<?> ret = m.getReturnType();
		
		Type rett;
		Type params[] = new Type[m.getParameterCount()];
		Class<?> oparams[] = m.getParameterTypes();
		rett = this.clz2type(ret);
		
		int j = 1;
		for(int i = 0; i < oparams.length; ++i) {
			Class<?> p = oparams[i];
			params[i] = clz2type(p);
			j += pushType(mv, p, j);
		}

		mv.visitMethodInsn(Opcodes.INVOKESTATIC, this.classMapping, value, Type.getMethodDescriptor(rett, params)); //TODO better method description parsing
		
		if(ret.isAnnotationPresent(ClassMapping.class)) {
			String v = ret.getAnnotation(ClassMapping.class).value();
			Method proxyCreator = Creator.PROXY_CREATOR;
			mv.visitLdcInsn(Type.getType(ret));
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Creator.class), proxyCreator.getName(), Type.getMethodDescriptor(proxyCreator), false);
	        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(ret));
	        mv.visitInsn(Opcodes.ARETURN);
		}else {
	        mv.visitInsn(this.returnOpcode(m.getReturnType()));
		}
		
        mv.visitMaxs(1, 1);
        mv.visitEnd();
	}
	
	private int pushType(MethodVisitor mv, Class<?> clz, int j) {
		Type type = this.clz2type(clz);

		if(clz.isAnnotationPresent(ClassMapping.class)) {
			Creator.proxyCreator(clz).pushI(mv, j, true);
		}else {
			mv.visitVarInsn(this.loadOpcode(type), j);
		}
		
		if(type == Type.LONG_TYPE || type == Type.DOUBLE_TYPE) return 2;
		else return 1;
	}

	private Type clz2type(Class<?> ret) {
		if(ret.isAnnotationPresent(ClassMapping.class)) {
			String v = ret.getAnnotation(ClassMapping.class).value();
			return Type.getType("L"+v+";");
		}else {
			return Type.getType(ret);
		}
	}

	public static ProxyCreator create(Class<?> clz) {
		return new ProxyCreator(clz);
	}
	
	public Constructor constructor = null;
	public Object construct(Object o) throws Exception {
		return this.constructor.newInstance(o);
	}
	
	public byte[] bytes() {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, 
        	_clzName,
        	null,
        	Type.getInternalName(ProxyParent.class),
        	new String[] {clz.getCanonicalName().replace('.', '/')}
        );
        cw.visitField(Opcodes.ACC_PRIVATE, PROXY_RAWINSTANCE_FIELDNAME, "L"+classMapping+";", null, null);
        
        this.constructor(cw);
        
        for(Method m : clz.getMethods()) {
        	if(AccessUtils.class.isAssignableFrom(clz) && m.getName().equalsIgnoreCase("_this")){
        		accessUtilsThis(cw, m);
        	}else if(AccessUtils.class.isAssignableFrom(clz) && m.getName().equalsIgnoreCase("_sCls")){
        		accessUtilsStaticClass(cw, m);
        	}else if(m.isAnnotationPresent(StaticFieldMapping.class)) {
        		if(m.getParameterCount() == 0) staticFieldGetter(cw, m);
        		else staticFieldSetter(cw, m);
        	}else if(m.isAnnotationPresent(FieldMapping.class)) {
        		if(m.getParameterCount() == 0) fieldGetter(cw, m);
        		else fieldSetter(cw, m);
        	}else if(m.isAnnotationPresent(MethodInvoker.class)) {
        		methodInvoker(cw, m);
        	}else if(m.isAnnotationPresent(StaticMethodInvoker.class)) {
        		staticMethodInvoker(cw, m);
        	}else {
        		//throw new RuntimeException("Unknown method found: "+m);
        	}
        }
        
        cw.visitEnd();
        
        
        /*try {
            byte[] bts = cw.toByteArray();
            FileOutputStream fos = new FileOutputStream(new File("/home/gh/test.class"));
            fos.write(bts);
            fos.close();
            return bts;
        }catch(Exception e) {
        	throw new RuntimeException(e);
        }*/
        byte[] bts = cw.toByteArray();
        return bts;
	}
}
