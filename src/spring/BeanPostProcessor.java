package spring;

public interface BeanPostProcessor {

    public void postProcessBeforeInitialization(Object bean, String beanName);
    public void postProcessAfterInitialization(Object bean, String beanName);
}
