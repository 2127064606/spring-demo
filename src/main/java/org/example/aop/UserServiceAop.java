package org.example.aop;

import org.example.service.UserService;
import org.example.spring.anntation.Component;
import org.example.spring.itf.BeanPostProcessor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class UserServiceAop implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if(!UserService.class.isAssignableFrom(bean.getClass()))return bean;
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if(!UserService.class.isAssignableFrom(bean.getClass()))return bean;
       Object proxy = Proxy.newProxyInstance(UserService.class.getClassLoader(), new Class[]{UserService.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        //执行前置通知
                        System.out.println("前置通知");
                        //调用目标对象的方法
                      Object result = method.invoke(bean, args);

                        //执行后置通知
                            System.out.println("后置通知");
                        return result;
                    }
                });
        return proxy;
    }
}
