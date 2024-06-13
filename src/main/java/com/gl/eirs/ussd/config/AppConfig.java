package com.gl.eirs.ussd.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class AppConfig {

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    int batchCount;

//    @Value("${file.path}")
//    String smsFilePath;
//
//    @Value("${ussd.file.path}")
//    String ussdFilePath;

    @Value("${process.type}")
    String processType;

//    @Value("${sms.file.pattern}")
//    String smsFilePattern;
//    @Value("${ussd.file.pattern}")
//    String ussdFilePattern;
    @Value("${alert.url}")
    String alertUrl;
    @Value("${initial.timer}")
    int initialTimer;

    @Value("${final.timer}")
    int finalTimer;

    @Value("${operator}")
    String operator;

//    @Value("${file.separator.parameter}")
//    private String fileSeparator;

    @Value("${move.file.path}")
    private String moveFilePath;

//    @Value("${sms.file.separator}")
//    private String smsFileSeparator;
//
//    @Value("${ussd.file.separator}")
//    private String ussdFileSeparator;
//
    @Value("${file.path}")
    String filePath;
    @Value("${file.separator.parameter}")
    String fileSeparatorParameter;

    @Value("${file.name.pattern}")
    String fileNamePattern;

    @Value("${error.file.path}")
    String errorFilePath;
}
