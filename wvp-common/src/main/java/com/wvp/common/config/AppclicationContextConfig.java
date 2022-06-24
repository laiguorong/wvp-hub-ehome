package com.wvp.common.config;

import org.springframework.context.ApplicationContext;


public class AppclicationContextConfig {

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    public static Object getBean(Class<?> objectClass) {
        Object bean = applicationContext.getBean(objectClass);
        return bean;
    }
}
