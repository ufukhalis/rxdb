package io.github.ufukhalis;

public class TestEntity {
    @Column(value = "id")
    private int id;

    @Column(value = "first")
    private String first;

    @Column(value = "last")
    private String last;

    @Column(value = "age")
    private int age;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getFirst() {
        return first;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getLast() {
        return last;
    }
}
