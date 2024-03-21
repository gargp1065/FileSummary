package com.gl.eirs.ussd;

import com.gl.eirs.ussd.config.AppConfig;
import com.gl.eirs.ussd.repository.app.UssdRepository;
import com.gl.eirs.ussd.service.FileService;
import com.gl.eirs.ussd.service.MainService;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableEncryptableProperties
public class UssdApplication implements CommandLineRunner {

    @Autowired
    MainService mainService;

    @Autowired
    AppConfig appConfig;

    public static void main(String[] args) {
        SpringApplication.run(UssdApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        mainService.processFile();
    }
}
