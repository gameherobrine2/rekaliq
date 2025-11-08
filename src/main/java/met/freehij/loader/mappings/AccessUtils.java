package met.freehij.loader.mappings;

public interface AccessUtils {
	public Object _this();
	public Class<?> _sCls();

	public default Class<?> _cls(){
		return _this().getClass();
	}
	
	public default <T> boolean _instanceof(Class<? extends AccessUtils> clz) {
		return Creator.proxy(null,  clz, false)._sCls().isInstance(_this());
	}
}
