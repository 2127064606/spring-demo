package org.example.config;


import org.example.spring.anntation.Component;
import org.example.spring.anntation.ComponentScan;
import org.example.spring.anntation.Configuration;

@ComponentScan(value = {"org.example.*"})
@Configuration
public class DemoConfig {

}
