package service;

import spring.Autowired;
import spring.BeanNameAware;
import spring.Component;
import spring.Scope;

@Component("userService")
public class UserService implements BeanNameAware {

    @Autowired
    private OrderService orderService;

    private String BeanName;
    @Override
    public void setBeanName(String beanName) {
        BeanName = beanName;
    }

    public void test(){
        System.out.println(orderService);
    }
}
