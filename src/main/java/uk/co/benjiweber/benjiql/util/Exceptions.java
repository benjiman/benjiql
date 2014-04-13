package uk.co.benjiweber.benjiql.util;

public class Exceptions {
    public interface ExceptionalSupplier<R> {
        public R apply() throws Exception;
    }

    public interface ExceptionalVoid<R> {
        public void apply() throws Exception;
    }

    public static <R> R unchecked(ExceptionalSupplier<R> f) {
        try {
            return f.apply();
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void unchecked(ExceptionalVoid f) {
        try {
            f.apply();
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
