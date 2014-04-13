package uk.co.benjiweber.benjiql.util;

import com.google.common.collect.ImmutableMap;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

public class Conventions {

    private static final Map<Class<?>, String> dbTypes = ImmutableMap.<Class<?>,String>builder()
        .put(String.class, "text")
        .put(Integer.class, "integer")
        .put(Float.class, "double")
        .put(Double.class, "double")
        .put(Long.class, "bigint")
        .put(Character.class, "integer")
        .put(Byte.class, "integer")
        .put(int.class, "integer")
        .put(float.class, "double")
        .put(double.class, "double")
        .put(long.class, "bigint")
        .put(char.class, "integer")
        .put(byte.class, "integer")
        .build();

    private static final Map<Class<?>, JDBCType> jdbcTypes = ImmutableMap.<Class<?>,JDBCType>builder()
        .put(String.class, JDBCType.VARCHAR)
        .put(Integer.class, JDBCType.INTEGER)
        .put(Float.class, JDBCType.FLOAT)
        .put(Double.class, JDBCType.DOUBLE)
        .put(Long.class, JDBCType.BIGINT)
        .put(Character.class, JDBCType.CHAR)
        .put(Byte.class, JDBCType.TINYINT)
        .put(int.class, JDBCType.INTEGER)
        .put(float.class, JDBCType.FLOAT)
        .put(double.class, JDBCType.DOUBLE)
        .put(long.class, JDBCType.BIGINT)
        .put(char.class, JDBCType.CHAR)
        .put(byte.class, JDBCType.TINYINT)
        .build();

    public interface JdbcSetter<T> {
        public void apply(PreparedStatement stmt, int index, T t) throws SQLException;
    }
    private static final Map<Class<?>, JdbcSetter> jdbcSetters = ImmutableMap.<Class<?>,JdbcSetter>builder()
        .put(String.class, (JdbcSetter<String>)PreparedStatement::setString)
        .put(Integer.class, (JdbcSetter<Integer>)PreparedStatement::setInt)
        .put(Float.class, (JdbcSetter<Float>)PreparedStatement::setFloat)
        .put(Double.class, (JdbcSetter<Double>) PreparedStatement::setDouble)
        .put(Long.class, (JdbcSetter<Long>)PreparedStatement::setLong)
        .put(Character.class, (JdbcSetter<Integer>)PreparedStatement::setInt)
        .put(Byte.class, (JdbcSetter<Integer>)PreparedStatement::setInt)
        .put(int.class, (JdbcSetter<Integer>)PreparedStatement::setInt)
        .put(float.class, (JdbcSetter<Float>)PreparedStatement::setFloat)
        .put(double.class, (JdbcSetter<Double>)PreparedStatement::setDouble)
        .put(long.class, (JdbcSetter<Long>)PreparedStatement::setLong)
        .put(char.class, (JdbcSetter<Integer>)PreparedStatement::setInt)
        .put(byte.class, (JdbcSetter<Integer>)PreparedStatement::setInt)
        .build();

    public static String toDbName(String name) {
        return uncapitalize(toSnakeCase(banishGetterSetters(name)));
    }

    public static String toDbName(Class<?>... clses) {
        return Arrays.asList(clses)
            .stream()
            .map(Class::getSimpleName)
            .map(Conventions::toDbName)
            .reduce((a,b) -> a + "_" + b)
            .orElse("");
    }

    public static String toJoinTableName(Class<?> cls, String name) {
        return toDbName(cls.getSimpleName()) + "_" + toDbName(name);
    }

    private static String banishGetterSetters(String name) {
        return name.replaceAll("^(get|set)", "");
    }

    public static String uncapitalize(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    public static String toSnakeCase(String s) {
        return s.replaceAll("([a-z])([A-Z])","$1_$2").toLowerCase();
    }

    public static <T> String toDbType(Class<T> cls) {
        return dbTypes.getOrDefault(cls, "text");
    }

    public static <T> JdbcSetter<T> getSetter(Class<T> cls) {
        return jdbcSetters.get(cls);
    }
}
