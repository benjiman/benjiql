package uk.co.benjiweber.benjiql.mocking;


import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import uk.co.benjiweber.benjiql.util.Conventions;

import java.lang.reflect.Method;

public class RecordingObject implements Answer {

    private String currentPropertyName = "";
    private Recorder<?> currentMock = null;

    @SuppressWarnings("unchecked")
    public static <T> Recorder<T> create(Class<T> cls) {

        final RecordingObject recordingObject = new RecordingObject();
        var mock = Mockito.mock(cls,recordingObject);
        return new Recorder((T) mock, recordingObject);
    }


    @Override
    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
        var method = invocationOnMock.getMethod();
        if (method.getName().equals("getCurrentPropertyName")) {
            return getCurrentPropertyName();
        }

        currentPropertyName = Conventions.toDbName(method.getName());
        try {
            currentMock = create(method.getReturnType());
            return currentMock.getObject();
        } catch (MockitoException e) {
            return DefaultValues.getDefault(method.getReturnType());
        }
    }

    public String getCurrentPropertyName() {
        return currentPropertyName + (currentMock == null ? "" : ("." + currentMock.getCurrentPropertyName()));
    }


}