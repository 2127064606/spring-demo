package org.example.service;


import org.example.dao.User;
import org.example.spring.anntation.Autowired;
import org.example.spring.anntation.Component;
import org.example.spring.anntation.Scope;
import org.example.spring.itf.BeanNameAware;


public interface UserService {
        void getInfo();
}
