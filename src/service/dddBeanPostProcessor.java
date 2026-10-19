package service;

import spring.BeanPostProcessor;
import spring.Component;

/*
    可以通过实现BeanPostProcessor接口来达到对特定Bean进行加工处理的功能
    需要添加Component注解
 */
@Component
public class dddBeanPostProcessor implements BeanPostProcessor {
    @Override
    public void postProcessBeforeInitialization(Object bean, String beanName) {
        if(beanName.equals("service")) {}
    }

    @Override
    public void postProcessAfterInitialization(Object bean, String beanName) {
    }
}
