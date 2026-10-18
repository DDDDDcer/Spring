package service;

import spring.*;

@Component("userService")
public class UserService implements BeanNameAware , InitializingBean {

    @Autowired
    private OrderService orderService;

    private String name;

    private String BeanName;
    @Override
    public void setBeanName(String beanName) {
        BeanName = beanName;
    }

    public void test(){
        System.out.println(orderService);
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("afterPropertiesSet");
    }
}
