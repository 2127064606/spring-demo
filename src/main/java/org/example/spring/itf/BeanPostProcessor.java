package org.example.spring.itf;

public interface BeanPostProcessor {
    //bean初始化前
    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return null;
    }


    //bean初始化后
    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return null;
    }
}
