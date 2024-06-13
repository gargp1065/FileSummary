package com.gl.eirs.ussd.service;

import com.gl.eirs.ussd.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class MainService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    AppConfig appConfig;
    @Autowired
    FileService fileService;

    public void processFile() throws Exception {
        logger.info("Starting the process of ussd/sms summary");
        String processType=appConfig.getProcessType().trim();
        if(processType.equalsIgnoreCase("ussd"))
            fileService.fileReadUssd();
        else if(processType.equalsIgnoreCase("sms"))
            fileService.fileReadSms();
        else {
            logger.error("Invalid file type in configuration");
        }
    }
}
