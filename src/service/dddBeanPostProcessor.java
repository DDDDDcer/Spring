package service;

import spring.BeanPostProcessor;
import spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/*
    可以通过实现BeanPostProcessor接口来达到对特定Bean进行加工处理的功能
    需要添加Component注解
 */
@Component
public class dddBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.printf("通过");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if(beanName.equals("userService")) {
            return Proxy.newProxyInstance(dddBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), (proxy, method, args) -> {
                System.out.println("切面");
                return method.invoke(bean, args);
            });
        }
        return bean;
    }
}
