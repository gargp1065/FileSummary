package com.gl.eirs.ussd.service;


import com.gl.eirs.ussd.config.AppConfig;
import com.gl.eirs.ussd.dto.FileDto;
import com.gl.eirs.ussd.entity.app.Sms;
import com.gl.eirs.ussd.repository.app.SmsRepository;
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
public class SmsService {

    @Autowired
    AppConfig appConfig;

    @Autowired
    SmsRepository smsRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public boolean processSmsFile(FileDto fileDto) {
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

                    String[] s = record.split(appConfig.getSmsFileSeparator(), -1);
                    if(s.length != 8) {
                        logger.error("The record length is not equal to 8 {}", Arrays.stream(s).toList());
                        continue;
                    }
                    Sms sms = new Sms();
                    sms.setFileName(fileDto.getFileName());
                    sms.setOperator(appConfig.getOperator());

                    sms.setMsisdn(s[0]);
                    java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf(s[1]);
                    sms.setSmsReceivedDate(timestamp.toLocalDateTime());
                    sms.setShortCode(s[2]);
                    sms.setRequestFromCustomer(s[3]);
                    timestamp = java.sql.Timestamp.valueOf(s[4]);
                    sms.setResponseDateTime(timestamp.toLocalDateTime());
                    sms.setResponse(s[5]);
                    sms.setRequestLang(s[6]);
                    sms.setResponseLang(s[7]);
                    try {
                        logger.info("Inserting the entry {}", sms);
                        smsRepository.save(sms);
                        succesCount++;
                    } catch (Exception ex) {
                        logger.error("The entry failed to save in sms summary table, {}", ex);
                        failureCount++;
                    }
                }
            } catch (Exception ex) {
                logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
                fileDto.setFailedRecords(failureCount);
                fileDto.setSuccessRecords(succesCount);
            }
//            fileDto.setFailedRecords(failureCount);
//            fileDto.setSuccessRecords(succesCount);
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
