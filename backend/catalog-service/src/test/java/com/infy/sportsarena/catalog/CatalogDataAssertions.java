package com.infy.sportsarena.catalog;

import org.assertj.core.api.Assertions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class CatalogDataAssertions {
    private CatalogDataAssertions() {}

    public static void verifyDataClass(Class<?> type) {
        try {
            Object sample = type.getDeclaredConstructor().newInstance();
            List<Field> fields = dataFields(type);
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = valueFor(field.getType(), 1);
                field.set(sample, value);
                String getter = (field.getType() == boolean.class ? "is" : "get")
                        + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                Assertions.assertThat(type.getMethod(getter).invoke(sample)).isEqualTo(value);
            }
            Object equal = copy(type, fields, sample);
            Assertions.assertThat(sample).isEqualTo(equal).hasSameHashCodeAs(equal);
            Assertions.assertThat(sample.toString()).isNotBlank();
            Assertions.assertThat(sample).isNotEqualTo(null).isNotEqualTo("different type");

            for (Field field : fields) {
                Object changed = copy(type, fields, sample);
                field.setAccessible(true);
                field.set(changed, valueFor(field.getType(), 2));
                Assertions.assertThat(sample).as(field.getName()).isNotEqualTo(changed);

                if (!field.getType().isPrimitive()) {
                    Object withNull = copy(type, fields, sample);
                    field.set(withNull, null);
                    Assertions.assertThat(sample).as(field.getName() + " null").isNotEqualTo(withNull);
                    withNull.hashCode();
                    Assertions.assertThat(withNull.toString()).isNotBlank();
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unable to test data class " + type.getName(), e);
        }
    }

    private static List<Field> dataFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) fields.add(field);
        }
        return fields;
    }

    private static Object copy(Class<?> type, List<Field> fields, Object source) throws ReflectiveOperationException {
        Object copy = type.getDeclaredConstructor().newInstance();
        for (Field field : fields) {
            field.setAccessible(true);
            field.set(copy, field.get(source));
        }
        return copy;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object valueFor(Class<?> type, int variant) throws ReflectiveOperationException {
        if (type == String.class) return "value-" + variant;
        if (type == Long.class || type == long.class) return (long) variant;
        if (type == Integer.class || type == int.class) return variant;
        if (type == BigDecimal.class) return BigDecimal.valueOf(variant);
        if (type == boolean.class || type == Boolean.class) return variant % 2 != 0;
        if (type == LocalDate.class) return LocalDate.of(2026, 1, variant);
        if (type.isEnum()) return type.getEnumConstants()[(variant - 1) % type.getEnumConstants().length];
        if (List.class.isAssignableFrom(type)) return List.of("value-" + variant);
        if (type.getName().startsWith("com.infy.sportsarena.catalog.")) {
            Object nested = type.getDeclaredConstructor().newInstance();
            for (Field field : dataFields(type)) {
                if (field.getType() == String.class) {
                    field.setAccessible(true);
                    field.set(nested, "nested-" + variant);
                }
            }
            return nested;
        }
        throw new IllegalArgumentException("No test value configured for " + type.getName());
    }
}
