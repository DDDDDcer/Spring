package service;

import spring.ApplicationContext;

public class Test {
    public static void main(String[] args) {
        ApplicationContext context = new ApplicationContext(AppConfig.class);

        UserService userService = (UserService) context.getBean("userService");
        userService.test();
    }
}
