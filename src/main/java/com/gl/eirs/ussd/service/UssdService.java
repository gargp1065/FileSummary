package com.gl.eirs.ussd.service;


import com.gl.eirs.ussd.config.AppConfig;
import com.gl.eirs.ussd.dto.FileDto;
import com.gl.eirs.ussd.entity.app.Ussd;
import com.gl.eirs.ussd.repository.app.UssdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

@Service
public class UssdService {

    @Autowired
    AppConfig appConfig;

    @Autowired
    UssdRepository ussdRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public boolean processUssdFile(FileDto fileDto) {
        int failureCount=0;
        int succesCount=0;
        try(BufferedReader reader = new BufferedReader(new FileReader(fileDto.getFilePath() +"/" + fileDto.getFileName()))) {

            try {
                String record;
                reader.readLine();
                while ((record = reader.readLine()) != null) {
                    if (record.isEmpty()) {
                        continue;
                    }
                    String[] s = record.split(appConfig.getUssdFileSeparator(), -1);
                    if(s.length != 9) {
                        logger.error("The record length is not equal to 9 {}", Arrays.stream(s).toList());
                        continue;
                    }
                    Ussd us = new Ussd();
//                System.out.println(Arrays.toString(s));
                    us.setFileName(fileDto.getFileName());
                    us.setOperator(appConfig.getOperator());
                    java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf(s[0]);
                    us.setRequestInitDate(timestamp.toLocalDateTime());
                    us.setRequestType(s[1]);
                    us.setUssdSessionId(s[2]);
                    us.setMsisdn(s[3]);
                    us.setImsi(s[4]);
                    us.setInput(s[5]);
                    java.sql.Timestamp timestamp1 = java.sql.Timestamp.valueOf(s[6]);
                    us.setSessionEndDateTime(timestamp1.toLocalDateTime());
                    us.setReasonSessionClose(s[7]);
                    us.setRespondLanguage(s[8]);
                    try {
                        logger.info("Inserting the entry {}", us);
                        ussdRepository.save(us);
                        succesCount++;
                    } catch (Exception ex) {
                        logger.error("The entry failed to save in ussd summary table, {}", ex);
                        failureCount++;
                    }
                }
            } catch (Exception ex) {
                logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
                fileDto.setFailedRecords(failureCount);
                fileDto.setSuccessRecords(succesCount);
            }
        } catch (FileNotFoundException ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
        } catch (Exception ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
        }

        fileDto.setFailedRecords(failureCount);
        fileDto.setSuccessRecords(succesCount);
        return false;
    }

}

