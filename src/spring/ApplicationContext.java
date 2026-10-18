package spring;

import service.Test;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext {

    private Class config;
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    //单例池
    private ConcurrentHashMap<String, Object> sigletonObjects = new ConcurrentHashMap<>();

    public ApplicationContext(Class config) {
        this.config = config;

        if(config.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = (ComponentScan) config.getAnnotation(ComponentScan.class);
            String packageName = config.getPackageName();

            packageName = packageName.replace(".", "/");

            ClassLoader classLoader = config.getClassLoader();
            URL resource = classLoader.getResource(packageName);

            File file = new File(resource.getFile());
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    String fileName = f.getAbsolutePath();
                    if(fileName.endsWith(".class")) {

                        String className = fileName.substring(fileName.indexOf("service"), fileName.indexOf(".class"));
                        className=className.replace("/", ".");
                        System.out.println(className);
                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            if (clazz.isAnnotationPresent(Component.class)) {

                                String name = clazz.getAnnotation(Component.class).value();
                                BeanDefinition beanDefinition = new  BeanDefinition();
                                if(clazz.isAnnotationPresent(Scope.class)) {
                                    beanDefinition.setScope(clazz.getAnnotation(Scope.class).value());
                                }else{
                                    beanDefinition.setScope("singleton");
                                }
                                beanDefinitionMap.put(name, beanDefinition);

                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {

                Object bean = createBean(beanName, beanDefinition);
                sigletonObjects.put(beanName, bean);
            }
        }
    }
    private Object createBean(String beanName,BeanDefinition beanDefinition) {
        return null;
    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition != null) {
            String scope = beanDefinition.getScope();
            //单例
            if (scope.equals("singleton")) {
                Object bean = sigletonObjects.get(beanName);
                if (bean == null) {
                    Object o = createBean(beanName, beanDefinition);
                    sigletonObjects.put(beanName, o);
                    return  bean;
                }
                return bean;
            }else{
                return createBean(beanName, beanDefinition);
            }
        }else {
            throw new NullPointerException();
        }

    }

}
