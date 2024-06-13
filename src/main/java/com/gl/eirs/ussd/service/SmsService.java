package com.gl.eirs.ussd.service;


import com.gl.eirs.ussd.config.AppConfig;
import com.gl.eirs.ussd.dto.FileDto;
import com.gl.eirs.ussd.entity.app.Sms;
import com.gl.eirs.ussd.entity.app.Ussd;
import com.gl.eirs.ussd.repository.app.SmsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;

@Service
public class SmsService {

    @Autowired
    AppConfig appConfig;

    @Autowired
    SmsRepository smsRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public boolean processSmsFile(FileDto fileDto, String operator) {
        int failureCount=0;
        int succesCount=0;
        try(BufferedReader reader = new BufferedReader(new FileReader(fileDto.getFilePath() +"/" + fileDto.getFileName()))) {
            File outFile = new File(appConfig.getErrorFilePath() + "/error_" + fileDto.getFileName());
            PrintWriter writer = new PrintWriter(outFile);
            try {

                String record;
                String[] header = reader.readLine().split(appConfig.getFileSeparatorParameter(), -1);
                if(operator.equalsIgnoreCase("CC")) {
                    if( header.length != 3 || !validateHeader(header, operator)) {
                        logger.error("Error header validation failed. Skipping the file");
                        fileDto.setFailedRecords(0);
                        fileDto.setSuccessRecords(0);
                        return true;
                    }
                } else {
                    if (header.length != 8 || !validateHeader(header, operator)) {
                        logger.error("Error header validation failed. Skipping the file");
                        fileDto.setFailedRecords(0);
                        fileDto.setSuccessRecords(0);
                        return true;
                    }
                }

                while ((record = reader.readLine()) != null) {
                    if (record.isEmpty()) {
                        continue;
                    }

                    String[] s = record.split(appConfig.getFileSeparatorParameter(), -1);


                    Sms sms = new Sms();

                    if(operator.equalsIgnoreCase("CC")) {
                        if(s.length != 3) {
                            logger.error("The record length is not equal to 3 {}", Arrays.stream(s).toList());
                            writer.println(record);
                            failureCount++;
                            continue;
                        }
                        sms = getSMSObject(s, operator.trim());
                    } else {
                        if(s.length != 8) {
                            logger.error("The record length is not equal to 9 {}", Arrays.stream(s).toList());
                            writer.println(record);
                            failureCount++;
                            continue;
                        }
                        sms = getSMSObject(s, operator.trim());
                    }

                    sms.setFileName(fileDto.getFileName());
                    sms.setOperator(appConfig.getOperator().trim());
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
                writer.close();
                return true;
            }
            writer.close();
//            fileDto.setFailedRecords(failureCount);
//            fileDto.setSuccessRecords(succesCount);
        } catch (FileNotFoundException ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
            return true;
        } catch (Exception ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
            return true;
        }

        fileDto.setFailedRecords(failureCount);
        fileDto.setSuccessRecords(succesCount);
        return false;
    }


    boolean validateHeader(String[] header, String operator) {
        logger.info("Validating the header of the file for SMS.");
        if(operator.equalsIgnoreCase("CC")) {
            if(header[0].trim().equalsIgnoreCase("time_stamp") && header[1].trim().equalsIgnoreCase("origination") &&
                    header[2].trim().equalsIgnoreCase("destination")) {
                return true;
            } else return false;
        } else {
            if(header[0].equalsIgnoreCase("msisdn") && header[1].equalsIgnoreCase("sms_recived_date")
                    && header[2].equalsIgnoreCase("shortcode")
                    && header[3].equalsIgnoreCase("request_from_customer")
                    && header[4].equalsIgnoreCase("respond_given_date") && header[5].equalsIgnoreCase("respond")
                    && header[6].equalsIgnoreCase("requested_language") && header[7].equalsIgnoreCase("respond_language")
            ) {
                return true;
            } else return false;
        }
    }

    public Sms getSMSObject(String[] s, String operator) {
        Sms sms = new Sms();
        if(operator.equalsIgnoreCase("CC")) {
            sms.setSmsReceivedDate((s[0] == "" ? null : s[0]));  //incase date is not available
            sms.setMsisdn(s[1].trim());
            sms.setShortCode(s[2].trim());
        }
        else  {
            sms.setMsisdn(s[0].trim());
//                    java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf(s[1]);
            sms.setSmsReceivedDate((s[1] == "" ? null : s[1]));  //incase date is not available
            sms.setShortCode(s[2].trim());
            sms.setRequestFromCustomer(s[3].trim());
//                    timestamp = java.sql.Timestamp.valueOf(s[4].trim());
            sms.setResponseDateTime((s[4] == "" ? null : s[4])); //incase date is not available
            sms.setResponse(s[5].trim());
            sms.setRequestLang(s[6].trim());
            sms.setResponseLang(s[7].trim());
        }
        return sms;

    }
}
