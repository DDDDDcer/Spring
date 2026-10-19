package spring;

import service.Test;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext {

    private Class config;
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    //单例池
    private ConcurrentHashMap<String, Object> sigletonObjects = new ConcurrentHashMap<>();
    private ArrayList<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public ApplicationContext(Class config) {
        this.config = config;

        if(config.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = (ComponentScan) config.getAnnotation(ComponentScan.class);

            String packageName = componentScan.value();
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

                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            if (clazz.isAnnotationPresent(Component.class)) {

                                //判断类是否实现了BeanPostProcessor接口,如果实现了这个接口将类放入处理列表中
                                if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                    BeanPostProcessor instance = (BeanPostProcessor) clazz.newInstance();
                                    beanPostProcessorList.add(instance);
                                }

                                String name = clazz.getAnnotation(Component.class).value();
                                //为Component注解为空时配置默认值
                                if(name.equals("")) {
                                    name = Introspector.decapitalize(clazz.getSimpleName());
                                }

                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(clazz);

                                if(clazz.isAnnotationPresent(Scope.class)) {
                                    beanDefinition.setScope(clazz.getAnnotation(Scope.class).value());
                                }else{
                                    beanDefinition.setScope("singleton");
                                }
                                beanDefinitionMap.put(name, beanDefinition);

                            }
                        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
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

    /*
    创建bean
     */
    private Object createBean(String beanName,BeanDefinition beanDefinition) {
        Class type = beanDefinition.getType();

        try {
            //1.实例化
            Object instance = type.getConstructor().newInstance();

            //2.依赖注入
            for (Field f : type.getDeclaredFields()) {
                if(f.isAnnotationPresent(Autowired.class)) {
                    f.setAccessible(true);
                    f.set(instance, getBean(f.getName()));
                }
            }

            //Aware回调
            if(instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            //初始化前处理bean
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            //初始化
            if(instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();
            }

            //初始化后处理bean
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }

            //BeanPostProcessor 初始化后 AOP


            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
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
