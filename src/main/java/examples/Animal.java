package examples;
import loader.Property;

public class Animal {
    @Property
    private int age;
    @Property(propertyName = "kittenName")
    private String name;
    @Property
    private String type;

    public void setAge(int age) {
        this.age = age;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Animal {" + "age=" + age + ", name='" + name + '\'' +
                ", type='" + type + '\'' + '}';
    }
}