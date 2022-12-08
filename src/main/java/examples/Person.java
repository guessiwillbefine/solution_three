package examples;

import loader.Property;

import java.time.Instant;

public class Person {
    @Property(propertyName = "age")
    private int age;
    @Property
    private Integer height;
    @Property(propertyName = "person_name")
    private String name;
    @Property(propertyName = "birthday",
            datePattern = Property.DateFormat.DOT,
            timePattern = Property.TimeFormat.COLON)
    private Instant birth;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getBirth() {
        return birth;
    }

    public void setBirth(Instant birth) {
        this.birth = birth;
    }
}
