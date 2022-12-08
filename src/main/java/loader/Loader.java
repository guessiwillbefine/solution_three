package loader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

/**
 * assignment 3.2 - Розробити класс-утиліту зі статичним методом, який приймає параметр типу Class і шлях до properties-файлу,
 * створює об'єкт цього класу, наповнює його атрибути значеннями з properties-файлу (викоистовуючи сеттери) і повертає цей об'єкт.
 */
public class Loader {

    private Loader() { /* only static methods so no use to create instances*/ }

    /**
     *
     * @param cls  Class of object that you need to build
     * @param propertiesPath  path to properties file
     * @return object of given class
     * @throws FileNotFoundException  if properties wasn't found
     * @throws NoSuchMethodException  if setter was not found for some reason
     * @throws IllegalArgumentException  if given param in properties can't be converted to field type
     */
    public static <T> T loadFromProperties(Class<T> cls, Path propertiesPath)
            throws FileNotFoundException, NoSuchMethodException, IllegalArgumentException {
        Optional<Properties> properties = findProperty(propertiesPath);
        T object;
        if (properties.isPresent()) {
            try {
                object = cls.getConstructor().newInstance();
                Field[] fields = cls.getDeclaredFields();
                for (Field field : fields) {
                    Optional<Method> setter = Arrays.stream(cls.getMethods())
                            .filter(x -> x.getName().startsWith("set" + beginWithUpper(field)))
                            .findFirst();
                    if (setter.isPresent()) {
                        Class<?>[] methodParams = setter.get().getParameterTypes();
                        invokeSetter(methodParams, object, setter.get(), field, properties.get());
                    }
                }
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new ReflectParsingException(e.getMessage());
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodException(e.getMessage());
            }
            return object;
        }
        throw new FileNotFoundException("Properties file was not found");
    }

    /**
     * @param field  Instant field
     * @param properties  .properties file
     * @return Instant object with given type
     * @throws IllegalArgumentException - if Date does not match given in @Property pattern or if date is not valid
     *
     */
    private static Instant unpackDate(Field field, Properties properties) throws IllegalArgumentException {
        Property.DateFormat datePattern = field.getAnnotation(Property.class).datePattern();
        Property.TimeFormat timePattern = field.getAnnotation(Property.class).timePattern();
        String[] propertyString = unpackProperties(field, properties).split(" ");
        if (propertyString[0].matches(datePattern.pattern)
                && propertyString[1].matches(timePattern.pattern)) {
            int[] date = Arrays.stream(propertyString[0]
                            .split(datePattern.separator))
                    .mapToInt(Integer::parseInt)
                    .toArray();

            int[] time = Arrays.stream(propertyString[1]
                            .split(timePattern.separator))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            try {
                return LocalDateTime.of(date[0], date[1], date[2], time[0], time[1]).toInstant(ZoneOffset.UTC);
            } catch (DateTimeException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        throw new IllegalArgumentException("given date does not match to chosen pattern");
    }

    /**
     *
     * @param field  Field object
     * @param properties  properties object where params are stored
     * @return unpacked params for this field, previously checks Property annotation for key
     */
    private static String unpackProperties(Field field, Properties properties) {
        String propertyName = field.getAnnotation(Property.class).propertyName();
        String key = field.getAnnotation(Property.class).key();
        if (!key.isBlank() && !propertyName.isBlank()) {
            String property = key + "." + propertyName;
            return properties.getProperty(property);
        }
        return properties.getProperty(propertyName.isBlank() ? field.getName() : propertyName);
    }

    private static void invokeSetter(Class<?>[] methodParameter, Object object,
                                     Method method, Field field, Properties properties)
            throws InvocationTargetException, IllegalAccessException, IllegalArgumentException {
        if (methodParameter.length == 1) {
            if (methodParameter[0].getTypeName().equals(int.class.getTypeName())
                    || methodParameter[0].getTypeName().equals(Integer.class.getName())) {
                int value = Integer.parseInt(unpackProperties(field, properties));
                method.invoke(object, value);
            } else if (methodParameter[0].getTypeName().equals(String.class.getName())) {
                method.invoke(object, unpackProperties(field, properties));
            } else if (methodParameter[0].getTypeName().equals(Instant.class.getName())) {
                method.invoke(object, unpackDate(field, properties));
            }
        } else throw new IllegalArgumentException("setter method must contain only one param");
    }

    /**
     * @param field  field need to be started with UpperCase
     * @return  name of the field with UpperCase first letter
     */
    private static String beginWithUpper(Field field) {
        return field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
    }

    /**
     * @param propertiesPath  path to .properties file
     * @return Properties object if file was found
     */
    private static Optional<Properties> findProperty(Path propertiesPath) {
        try (Reader reader = new FileReader(propertiesPath.toFile())) {
            Properties properties = new Properties();
            properties.load(reader);
            return Optional.of(properties);
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
