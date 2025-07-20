package org.example.dao;

import org.example.spring.anntation.Component;
import org.example.spring.anntation.ComponentScan;
import org.example.spring.anntation.Scope;
import org.example.spring.itf.BeanNameAware;

@Component
public class User implements BeanNameAware {
    String name;
    int age;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
        @Override
    public String toString() {
        return "User [name=" + name + ", age=" + age + "]";
    }

    @Override
    public void setBeanName(String name) {
       // System.out.println("BeanNameAware: Bean name is " + name);
    }
}
