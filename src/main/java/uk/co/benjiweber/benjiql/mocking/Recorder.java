package uk.co.benjiweber.benjiql.mocking;


import uk.co.benjiweber.benjiql.mocking.RecordingObject;

public class Recorder<T> {

    private T t;
    private RecordingObject recorder;

    public Recorder(T t, RecordingObject recorder) {
        this.t = t;
        this.recorder = recorder;
    }

    public String getCurrentPropertyName() {
        return recorder.getCurrentPropertyName();
    }

    public T getObject() {
        return t;
    }
}