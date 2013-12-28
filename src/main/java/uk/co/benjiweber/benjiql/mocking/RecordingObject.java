package uk.co.benjiweber.benjiql.mocking;


import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import uk.co.benjiweber.benjiql.util.Conventions;

import java.lang.reflect.Method;

public class RecordingObject implements MethodInterceptor {

    private String currentPropertyName = "";
    private Recorder<?> currentMock = null;

    @SuppressWarnings("unchecked")
    public static <T> Recorder<T> create(Class<T> cls) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(cls);
        final RecordingObject recordingObject = new RecordingObject();

        enhancer.setCallback(recordingObject);
        return new Recorder((T) enhancer.create(), recordingObject);
    }

    public Object intercept(Object o, Method method, Object[] os, MethodProxy mp) throws Throwable {
        if (method.getName().equals("getCurrentPropertyName")) {
            return getCurrentPropertyName();
        }
        currentPropertyName = Conventions.toDbName(method.getName());
        try {
            currentMock = create(method.getReturnType());
            return currentMock.getObject();
        } catch (IllegalArgumentException e) {
            return DefaultValues.getDefault(method.getReturnType());
        }
    }

    public String getCurrentPropertyName() {
        return currentPropertyName + (currentMock == null ? "" : ("." + currentMock.getCurrentPropertyName()));
    }
}