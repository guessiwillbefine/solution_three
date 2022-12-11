import examples.Person;
import loader.Loader;
import loader.Property;
import examples.Animal;
import org.junit.jupiter.api.Test;
import java.io.FileNotFoundException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LoaderTest {

    @Test
    void testValidInput() throws FileNotFoundException, NoSuchMethodException, IllegalArgumentException {
            Path validPath = Path.of("src/test/resources/valid/person.properties");
            Person person = Loader.loadFromProperties(Person.class, validPath);
            assertNotNull(person);
            assertNotNull(person.getName());
            assertNotEquals(0, person.getAge());
            assertNotNull(person.getBirth());
            assertNotNull(person.getHeight());
            Path validPath2 = Path.of("src/test/resources/valid/animal.properties");
            Animal animal = Loader.loadFromProperties(Animal.class, validPath2);
            assertNotNull(animal);
            assertNotNull(animal.getName());
            assertNotNull(animal.getName());
            assertNotNull(animal.getType());
            assertNotEquals(0, animal.getAge());
    }
    @Test
    void testInvalidInput() {
        Path validPath = Path.of("src/test/resources/invalid/person_illegalArg.properties");
        assertThrows(IllegalArgumentException.class,
                ()->Loader.loadFromProperties(Person.class, validPath));
    }

    @Test
    void noSetterShouldThrowException() {
        class NoSetterClass {
            @Property
            private int value;
        }
        Path path = Path.of("src/test/resources/invalid/noSetter.properties");
        assertThrows(NoSuchMethodException.class,
                ()-> Loader.loadFromProperties(NoSetterClass.class, path));
    }
    @Test
    void propertiesNotFoundCase() {
        Path validPath = Path.of("src/test/resources/invalid/none.properties");
        assertThrows(FileNotFoundException.class,
                ()-> Loader.loadFromProperties(Person.class, validPath));
    }

}
