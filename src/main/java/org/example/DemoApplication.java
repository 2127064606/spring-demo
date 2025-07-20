package org.example;

import org.example.config.DemoConfig;
import org.example.service.UserService;
import org.example.service.impl.UserServiceImpl;
import org.example.spring.DemoAnnotationApplicationContext;


public class DemoApplication {

    public static void main(String[]args){
       DemoAnnotationApplicationContext ctx = new DemoAnnotationApplicationContext(DemoConfig.class);

        UserService userService = (UserService) ctx.getBean(UserService.class);
        System.out.println(userService.getClass().getName());
        userService.getInfo();

    }


}
