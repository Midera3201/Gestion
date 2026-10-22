package com.ecole.sirh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point de depart de l'application.
 *
 * @SpringBootApplication regroupe 3 annotations :
 *  - @Configuration      : cette classe peut declarer des beans
 *  - @EnableAutoConfiguration : Spring Boot configure Tomcat, la datasource,
 *                              Hibernate, Thymeleaf... tout seul
 *  - @ComponentScan      : Spring cherche les classes @Controller, @Service,
 *                          @Repository dans com.ecole.sirh et ses sous-paquets
 *
 * C'est pour cela que TOUT notre code doit rester sous com.ecole.sirh.
 */
@SpringBootApplication
public class SirhApplication {

    public static void main(String[] args) {
        SpringApplication.run(SirhApplication.class, args);
    }
}
