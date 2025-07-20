package org.example.service;

import org.example.spring.anntation.Component;
import org.example.spring.itf.BeanNameAware;
import org.example.spring.itf.BeanPostProcessor;
import org.example.spring.itf.InitializeBean;


//@Component
public class UserBeanPostProcessor implements BeanPostProcessor, InitializeBean, BeanNameAware {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if(!bean.getClass().equals(UserBeanPostProcessor.class))return bean;
        System.out.println("Before initialization of bean: " + beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if(!bean.getClass().equals(UserBeanPostProcessor.class))return bean;
        System.out.println("After initialization of bean: " + beanName);
        return bean;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Initializing UserBeanPostProcessor");
    }

    @Override
    public void setBeanName(String name) {
        System.out.println("Setting bean name: " + name);
    }
}
