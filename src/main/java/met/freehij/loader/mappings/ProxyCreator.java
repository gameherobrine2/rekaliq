package met.freehij.loader.mappings;

import java.io.File;
import java.io.FileOutputStream;
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
		else if(clz == int.class || clz == short.class || clz == byte.class) return Opcodes.IRETURN;
        else if(clz == long.class) return (Opcodes.LRETURN);
        else if(clz == float.class) return Opcodes.FRETURN;
        else if(clz == double.class) return Opcodes.DRETURN;
        else {
            return Opcodes.ARETURN;
        }
	}
	
	private int loadOpcode(Class<?> clz) {
		if(clz == int.class || clz == short.class || clz == byte.class) return Opcodes.ILOAD;
        else if(clz == long.class) return (Opcodes.LLOAD);
        else if(clz == float.class) return Opcodes.FLOAD;
        else if(clz == double.class) return Opcodes.DLOAD;
        else {
            return Opcodes.ALOAD;
        }
	}
	
	private void rawAccessThis(ClassWriter cw, Method m) {
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, m.getName(), Type.getMethodDescriptor(m), null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitFieldInsn(Opcodes.GETFIELD, this._clzName, PROXY_RAWINSTANCE_FIELDNAME, "L"+this.classMapping+";");
        mv.visitInsn(this.returnOpcode(m.getReturnType()));
        mv.visitMaxs(1, 1);
        mv.visitEnd();
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
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitTypeInsn(Opcodes.CHECKCAST, Creator.proxyCreator(clz)._clzName);
			mv.visitFieldInsn(Opcodes.GETFIELD, Creator.proxyCreator(clz)._clzName, PROXY_RAWINSTANCE_FIELDNAME, "L"+clz.getAnnotation(ClassMapping.class).value()+";");
			mv.visitFieldInsn(Opcodes.PUTSTATIC, classMapping, value, "L"+clz.getAnnotation(ClassMapping.class).value()+";");
			Method proxyCreator = Creator.PROXY_CREATOR;
			mv.visitFieldInsn(Opcodes.GETSTATIC, classMapping, value, "L"+v+";");
			mv.visitLdcInsn(Type.getType(this.clz));
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
			mv.visitLdcInsn(Type.getType(this.clz));
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Creator.class), proxyCreator.getName(), Type.getMethodDescriptor(proxyCreator), false);
	        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(this.clz));
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
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, this._clzName, PROXY_RAWINSTANCE_FIELDNAME, "L"+this.classMapping+";");
			mv.visitFieldInsn(Opcodes.GETFIELD, classMapping, value, "L"+v+";");
			mv.visitLdcInsn(Type.getType(this.clz));
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Creator.class), proxyCreator.getName(), Type.getMethodDescriptor(proxyCreator), false);
	        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(this.clz));
		}else {
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, this._clzName, PROXY_RAWINSTANCE_FIELDNAME, "L"+this.classMapping+";");
			mv.visitFieldInsn(Opcodes.GETFIELD, classMapping, value, Type.getDescriptor(clz));
		}
        mv.visitInsn(this.returnOpcode(m.getReturnType()));
        mv.visitMaxs(1, 1);
        mv.visitEnd();
	}
	
	private void fieldSetter(ClassWriter cw, Method m) {
		if(m.getReturnType() != m.getParameterTypes()[0]) {
			throw new RuntimeException(String.format("Method return type(%s) does not match parameter 1(%s)!", m.getReturnType(), m.getParameterTypes()[0]));
		}
		
		String value = m.getAnnotation(FieldMapping.class).value();
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, m.getName(), Type.getMethodDescriptor(m), null, null);
		mv.visitCode();
		Class<?> clz = m.getReturnType();

		
		if(clz.isAnnotationPresent(ClassMapping.class)) { //returns a reference to another class mapping - must access Proxy.i & create a new proxy<?>
			String v = clz.getAnnotation(ClassMapping.class).value();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, this._clzName, PROXY_RAWINSTANCE_FIELDNAME, "L"+this.classMapping+";");
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitTypeInsn(Opcodes.CHECKCAST, Creator.proxyCreator(clz)._clzName);
			mv.visitFieldInsn(Opcodes.GETFIELD, Creator.proxyCreator(clz)._clzName, PROXY_RAWINSTANCE_FIELDNAME, "L"+clz.getAnnotation(ClassMapping.class).value()+";");
			mv.visitFieldInsn(Opcodes.PUTFIELD, classMapping, value, "L"+clz.getAnnotation(ClassMapping.class).value()+";");
			
			Method proxyCreator = Creator.PROXY_CREATOR;
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, this._clzName, PROXY_RAWINSTANCE_FIELDNAME, "L"+this.classMapping+";");
			mv.visitFieldInsn(Opcodes.GETFIELD, classMapping, value, "L"+v+";");
			mv.visitLdcInsn(Type.getType(this.clz));
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Creator.class), proxyCreator.getName(), Type.getMethodDescriptor(proxyCreator), false);
	        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(this.clz));
		}else {
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, this._clzName, PROXY_RAWINSTANCE_FIELDNAME, "L"+this.classMapping+";");
			mv.visitVarInsn(loadOpcode(m.getReturnType()), 1);
			mv.visitFieldInsn(Opcodes.PUTFIELD, classMapping, value, Type.getDescriptor(clz));
			
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, this._clzName, PROXY_RAWINSTANCE_FIELDNAME, "L"+this.classMapping+";");
			mv.visitFieldInsn(Opcodes.GETFIELD, classMapping, value, Type.getDescriptor(clz));
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
		Class<?> clz = m.getReturnType();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitFieldInsn(Opcodes.GETFIELD, this._clzName, PROXY_RAWINSTANCE_FIELDNAME, "L"+this.classMapping+";");
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.classMapping, m.getName(), Type.getMethodDescriptor(m)); //TODO better method description parsing
		
        mv.visitInsn(this.returnOpcode(m.getReturnType()));
        mv.visitMaxs(1, 1);
        mv.visitEnd();
	}
	
	public void staticMethodInvoker(ClassWriter cw, Method m) {
		//if(m.getReturnType() == void.class) {
		//	
		//}
		String value = m.getAnnotation(StaticMethodInvoker.class).value();
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, m.getName(), Type.getMethodDescriptor(m), null, null);
		mv.visitCode();
		Class<?> clz = m.getReturnType();
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, this.classMapping, m.getName(), Type.getMethodDescriptor(m)); //TODO better method description parsing
		
        mv.visitInsn(this.returnOpcode(m.getReturnType()));
        mv.visitMaxs(1, 1);
        mv.visitEnd();
	}
	
	public static ProxyCreator create(Class<?> clz) {
		return new ProxyCreator(clz);
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
        	if(RawAccess.class.isAssignableFrom(clz) && m.getName().equalsIgnoreCase("_this")){
        		rawAccessThis(cw, m);
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
        		throw new RuntimeException("Unknown method found: "+m);
        	}
        }
        
        cw.visitEnd();
        
        
        try {
            byte[] bts = cw.toByteArray();
            FileOutputStream fos = new FileOutputStream(new File("/home/gh/test.class"));
            fos.write(bts);
            fos.close();
            return bts;
        }catch(Exception e) {
        	throw new RuntimeException(e);
        }
	}
}
