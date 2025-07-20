package org.example.service.impl;

import org.example.service.UserService;
import org.example.spring.anntation.Component;

@Component
public class UserServiceImpl implements UserService {

    @Override
    public void getInfo() {
        System.out.println("User Service Implementation");
    }
}
