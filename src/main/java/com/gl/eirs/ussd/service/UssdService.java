package com.gl.eirs.ussd.service;


import com.gl.eirs.ussd.config.AppConfig;
import com.gl.eirs.ussd.dto.FileDto;
import com.gl.eirs.ussd.entity.app.Ussd;
import com.gl.eirs.ussd.repository.app.UssdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.Timestamp;
import java.util.Arrays;

@Service
public class UssdService {

    @Autowired
    AppConfig appConfig;

    @Autowired
    UssdRepository ussdRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public boolean  processUssdFile(FileDto fileDto, String operator) {
        int failureCount=0;
        int succesCount=0;
        try(BufferedReader reader = new BufferedReader(new FileReader(fileDto.getFilePath() +"/" + fileDto.getFileName()))) {
            File outFile = new File(appConfig.getErrorFilePath() + "/error_" + fileDto.getFileName());
            PrintWriter writer = new PrintWriter(outFile);
            try {
                String record;
                String[] header = reader.readLine().split(appConfig.getFileSeparatorParameter(), -1);
                if(operator.equalsIgnoreCase("CC")) {
                    if( header.length != 4 || !validateHeader(header, operator)) {
                        logger.error("Error header validation failed. Skipping the file");
                        fileDto.setFailedRecords(0);
                        fileDto.setSuccessRecords(0);
                        return true;
                    }
                } else {
                    if (header.length != 9 || !validateHeader(header, operator)) {
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
                    Ussd ussd = null;
                    if(operator.equalsIgnoreCase("CC")) {
                        if(s.length != 4) {
                            logger.error("The record length is not equal to 4 {}", Arrays.stream(s).toList());
                            writer.println(record);
                            failureCount++;
                            continue;
                        }
                        ussd = getUssdObject(s, operator.trim());
                    } else {
                        if(s.length != 9) {
                            logger.error("The record length is not equal to 9 {}", Arrays.stream(s).toList());
                            writer.println(record);
                            failureCount++;
                            continue;
                        }
                        ussd = getUssdObject(s, operator.trim());
                    }
                    ussd.setFileName(fileDto.getFileName());
                    try {
                        logger.info("Inserting the entry {}", ussd);
                        ussdRepository.save(ussd);
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
                writer.close();
                return true;
            }
            writer.close();
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
        logger.info("Validating the header of the file for USSD.");
        if(operator.equalsIgnoreCase("CC")) {
            if(header[0].trim().equalsIgnoreCase("time_stamp") && header[1].trim().equalsIgnoreCase("origination") &&
                    header[2].trim().equalsIgnoreCase("destination") && header[3].trim().equalsIgnoreCase("ussd_sessionid")) {
                return true;
            } else return false;
        }
        else {
            if (header[0].trim().equalsIgnoreCase("session_init_date") && header[1].trim().equalsIgnoreCase("request_type")
                    && header[2].trim().equalsIgnoreCase("ussd_sessionid")
                    && header[3].trim().equalsIgnoreCase("msisdn")
                    && header[4].trim().equalsIgnoreCase("imsi") && header[5].trim().equalsIgnoreCase("inputs")
                    && header[6].trim().equalsIgnoreCase("session_end_date") && header[7].equalsIgnoreCase("reason_session_close")
                    && header[8].trim().equalsIgnoreCase("respond_language")
            ) {
                return true;
            } else return false;
        }
    }

    public Ussd getUssdObject(String[] s, String operator) {
        Ussd ussd = new Ussd();
        ussd.setOperator(operator);
        ussd.setRequestInitDate(s[0] == "" ? null : s[0].trim()); //incase date is not available
        if(operator.equalsIgnoreCase("CC")) {
            ussd.setMsisdn(s[1].trim());
            ussd.setUssdSessionId(s[3].trim());

        }
        else  {
            ussd.setRequestType(s[1].trim().trim());
            ussd.setUssdSessionId(s[2].trim());
            ussd.setMsisdn(s[3].trim());
            ussd.setImsi(s[4].trim());
            ussd.setInput(s[5].trim());
            ussd.setSessionEndDateTime(s[6] == "" ? null : s[6].trim()); //incase date is not available
            ussd.setReasonSessionClose(s[7].trim());
            ussd.setRespondLanguage(s[8].trim());
        }
        return ussd;

    }
}

