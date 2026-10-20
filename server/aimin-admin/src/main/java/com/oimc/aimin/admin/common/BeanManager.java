package com.oimc.aimin.admin.common;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/*
 *
 * @author 渣哥
 */
@Getter
@Slf4j
public class BeanManager {
    private static ApplicationContext applicationContext;

    public static void setAppContext(ApplicationContext appContext) {
        BeanManager.applicationContext = appContext;
    }
    /**
     * 通过Bean名称获取
     */
    public static Object getBean(String name) {
        try {
            return applicationContext.getBean(name);
        } catch (NoSuchBeanDefinitionException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 通过Bean类型获取
     * 如果存在同一类型的多个Bean实例则会抛出NoSuchBeanDefinitionException，异常信息：expected single matching bean but found 2
     */
    public static <T> T getBean(Class<T> clazz) {
        try {
            return applicationContext.getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 通过Bean名称和类型获取
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        try {
            return applicationContext.getBean(name, clazz);
        } catch (NoSuchBeanDefinitionException e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
