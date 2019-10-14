package com.nan.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyClass {
    public static void main(String[] args) {
        List<Person> personList = new ArrayList<>();
        personList.add(new Person("aa"));
        personList.add(new Person("bb"));
        personList.add(new Person("cc"));
        personList.add(new Person("dd"));

//        System.out.println("personList contains = " + personList.contains(new Person("aa")));

        List<String> list = new ArrayList<>();
        Map<String, String> map = new HashMap<>();

        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");

        map.put("A", "a");

        for (String s:list) {
            if (map.get(s) == null) {
                System.out.println(s);
            } else {
                System.out.println(map.get(s));
            }
        }

        float a = 12333.23F;
        int m = (int) (a / 60);
        int s = (int) (a % 60);
        System.out.println(String.format("%d:%02d", m, s));
    }
}
