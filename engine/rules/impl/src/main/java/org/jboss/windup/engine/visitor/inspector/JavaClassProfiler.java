package org.jboss.windup.engine.visitor.inspector;

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.Type;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.dao.JavaMethodDao;
import org.jboss.windup.graph.model.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an Apache BCEL Class Visitor, in order to read Bytecode details of the Java Class.
 *  
 * @author bradsdavis
 *
 */
public class JavaClassProfiler extends EmptyVisitor {
    private static final Logger LOG = LoggerFactory.getLogger(JavaClassProfiler.class);

    private final JavaClass javaClass;
    private final JavaMethodDao javaMethodDao;
    private final JavaClassDao javaClassDao;
    private org.jboss.windup.graph.model.resource.JavaClass current;
    private final Resource resource;
    
    public JavaClassProfiler(JavaClass clz, JavaClassDao javaClassDao, JavaMethodDao methodDao, Resource resource) {
        this.javaClass = clz;
        this.javaClassDao = javaClassDao;
        this.resource = resource;
        this.javaMethodDao = methodDao;
    }

    public void process() {
        this.javaClass.accept(this);
    }
    
    @Override
    public void visitJavaClass(JavaClass obj) {
        current = javaClassDao.createJavaClass(obj.getClassName());
        int major = obj.getMajor();
        int minor = obj.getMinor();
        current.setMajorVersion(major);
        current.setMinorVersion(minor);
        current.addResource(resource);
        current.setPackageName(obj.getPackageName());
        
        for(String interfaceName : obj.getInterfaceNames()) {
            org.jboss.windup.graph.model.resource.JavaClass interfaceClass = javaClassDao.createJavaClass(interfaceName);
            //then we make the connection.
            current.addImplements(interfaceClass);
        }
        
        /** For full method profiling, uncomment.  This is expensive time and disk wise.
        for(Method method : obj.getMethods()) {
            JavaMethod jm = javaMethodDao.createJavaMethod(current, method.getName(), toJavaClasses(method.getArgumentTypes()));
            jm.setMethodName(method.getName());
            current.addJavaMethod(jm);
        }
        **/
        
        //process the pool.
        Constant[] pool = obj.getConstantPool().getConstantPool();
        for(Constant c : pool) {
            if(c == null) continue;
            c.accept(this);
        } 
        
        String superClz = obj.getSuperclassName();
        org.jboss.windup.graph.model.resource.JavaClass superJavaClass = javaClassDao.createJavaClass(superClz);
        current.setExtends(superJavaClass);
    }
    
    protected org.jboss.windup.graph.model.resource.JavaClass[] toJavaClasses(Type[] types) {
        org.jboss.windup.graph.model.resource.JavaClass[] clz = new org.jboss.windup.graph.model.resource.JavaClass[types.length];
        
        
        for(int i=0, j=types.length; i<j; i++) {
            Type t = types[i];
            clz[i] = javaClassDao.createJavaClass(t.toString());
        }
        
        return clz;
    }
    
    @Override
    public void visitConstantClass(ConstantClass obj) {
        ConstantPool pool = javaClass.getConstantPool();
        String classVal = obj.getConstantValue(pool).toString();
        classVal = StringUtils.replace(classVal, "/", ".");

        if(StringUtils.equals(classVal, this.javaClass.getClassName())) {
            //skip adding class name.
            return;
        }
        
        org.jboss.windup.graph.model.resource.JavaClass clz = javaClassDao.createJavaClass(classVal);
        current.addImport(clz);
    }
    
}
