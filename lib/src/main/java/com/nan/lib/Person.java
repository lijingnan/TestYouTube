package com.nan.lib;

/**
 * Author:jingnan
 * Time:2019-09-21/10
 */
public class Person {
    private String name;

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Person) {
            return name.equals(((Person) o).name);
        }
        return super.equals(o);
    }
}
