package com.gl.eirs.ussd.service;


import com.gl.eirs.ussd.alert.AlertService;
import com.gl.eirs.ussd.builder.ModulesAuditTrailBuilder;
import com.gl.eirs.ussd.config.AppConfig;
import com.gl.eirs.ussd.config.AppDbConfig;
import com.gl.eirs.ussd.dto.FileDto;
import com.gl.eirs.ussd.entity.app.Ussd;
import com.gl.eirs.ussd.entity.aud.ModulesAuditTrail;
import com.gl.eirs.ussd.repository.app.SmsRepository;
import com.gl.eirs.ussd.repository.app.UssdRepository;
import com.gl.eirs.ussd.repository.aud.ModulesAuditTrailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.gl.eirs.ussd.constants.Constants.*;


@Service
public class FileService {

    @Autowired
    UssdRepository ussdRepository;
    @Autowired
    SmsRepository smsRepository;

    @Autowired
    AppConfig appConfig;

    @Autowired
    AppDbConfig appDbConfig;

    @Autowired
    FileServiceUtils fileServiceUtils;

    @Autowired
    AlertService alertService;

    @Autowired
    ModulesAuditTrailRepository modulesAuditTrailRepository;
    @Autowired
    ModulesAuditTrailBuilder modulesAuditTrailBuilder;

    @Autowired
    SmsService smsService;
    @Autowired
    UssdService ussdService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//    @Autowired

    public void fileReadUssd() throws Exception {

        // check if process already executed for today
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String statusCode = modulesAuditTrailRepository.getStatusCode(ussdFeatureName,ussdModuleName + appConfig.getOperator(), sdf.format(date));
        if(statusCode != null && statusCode.equalsIgnoreCase("200")) {
            logger.info("Process already completed for the day for USSD.");
            return;
        }

        try {
            ArrayList<FileDto> fileDtos = fileServiceUtils.getFiles(appConfig.getFilePath(), appConfig.getFileNamePattern());
            logger.info("The count of files is {}", fileDtos.size());
            if (fileDtos.isEmpty()) {
                logger.error("No files found. Raising an alert");
                alertService.raiseAnAlert("alert5500", "", "", 0);
                return;
            }
            for (FileDto fileDto : fileDtos) {
                int moduleAuditId = 0;
                long startTime = System.currentTimeMillis();
                try {

                    logger.info("Processing the file {}", fileDto.getFileName());
                    fileServiceUtils.checkFileUploaded(fileDto);

                    // create modules_audit_trail entry for this file.
                    ModulesAuditTrail modulesAuditTrail = modulesAuditTrailBuilder.forInsert(201, "INITIAL", "NA", ussdModuleName + appConfig.getOperator(), ussdFeatureName, "", fileDto.getFileName(), LocalDateTime.now());
                    ModulesAuditTrail entity = modulesAuditTrailRepository.save(modulesAuditTrail);
                    moduleAuditId = entity.getId();
                    String fullFileName = fileDto.getFilePath() + fileDto.getFileName();
                    if(ussdService.processUssdFile(fileDto, appConfig.getOperator().trim())) {
                        logger.error("Processing failed for file ussd summary.");
                        logger.info("The summary for file {} is {}", fileDto.getFileName(), fileDto);
                        modulesAuditTrailRepository.updateModulesAudit(501, "FAIL", "Processing failed for ussd summary.", (int) fileDto.getTotalRecords(), (int)fileDto.getFailedRecords(), (int) (System.currentTimeMillis() - startTime), LocalDateTime.now(), (int)fileDto.getSuccessRecords(), moduleAuditId);
                        alertService.raiseAnAlert("alert5501", fileDto.getFileName(), appConfig.getOperator(), 0);
                        fileServiceUtils.moveFile(fileDto, appConfig.getMoveFilePath());
                        continue;
                    }
                    modulesAuditTrailRepository.updateModulesAudit(200, "SUCCESS", "NA", (int) fileDto.getTotalRecords(), (int) fileDto.getFailedRecords(), (int) (System.currentTimeMillis() - startTime), LocalDateTime.now(), (int) fileDto.getSuccessRecords(),moduleAuditId);
                    fileServiceUtils.moveFile(fileDto, appConfig.getMoveFilePath());
                    logger.info("The summary for file {} is {}", fileDto.getFileName(), fileDto);
                } catch (Exception e) {
                    logger.error("Processing failed for file ussd summary due to error {}", e.getMessage());
                    logger.info("The summary for file {} is {}", fileDto.getFileName(), fileDto);
                    modulesAuditTrailRepository.updateModulesAudit(501, "FAIL", "Processing failed for ussd summary due to error " + e.getMessage(), (int) fileDto.getTotalRecords(), (int)fileDto.getFailedRecords(), (int) (System.currentTimeMillis() - startTime), LocalDateTime.now(), (int)fileDto.getSuccessRecords(), moduleAuditId);
                    alertService.raiseAnAlert("alert5501", fileDto.getFileName(), appConfig.getOperator(), 0);

                }
            }
        } catch (Exception e) {
            logger.error("Processing failed for ussd summary due to error {}", e.getMessage());
//            modulesAuditTrailRepository.updateModulesAudit(501, "FAIL", "TProcessing failed for ussd summary due to error " +e.getMessage(), (int)fileDto.getExceptionListSuccess() + (int)fileDto.getBlackListSuccess() + (int)fileDto.getGreyListSuccess(), (int) fileDto.getBlackListFailure() +  (int) fileDto.getGreyListFailure() + (int) fileDto.getExceptionListFailure() , (int) (System.currentTimeMillis() - startTime), LocalDateTime.now(), moduleAuditId);
            alertService.raiseAnAlert("alert5502", "", "", 0);
        }

    }

    public void fileReadSms() throws Exception {

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String statusCode = modulesAuditTrailRepository.getStatusCode(smsFeatureName,smsModuleName + appConfig.getOperator(), sdf.format(date));
        if(statusCode != null && statusCode.equalsIgnoreCase("200")) {
            logger.info("Process already completed for the day for SMS.");
            return;
        }
        try {
            ArrayList<FileDto> fileDtos = fileServiceUtils.getFiles(appConfig.getFilePath(), appConfig.getFileNamePattern());
            logger.info("The count of files is {}", fileDtos.size());
            if (fileDtos.isEmpty()) {
                logger.error("No files found. Raising an alert");
                alertService.raiseAnAlert("alert5600", "", "", 0);
                return;
            }

            for (FileDto fileDto : fileDtos) {
                int moduleAuditId = 0;
                long startTime = System.currentTimeMillis();
                try {


                    logger.info("Processing the file {}", fileDto.getFileName());
                    fileServiceUtils.checkFileUploaded(fileDto);

                    // create modules_audit_trail entry for this file.
                    ModulesAuditTrail modulesAuditTrail = modulesAuditTrailBuilder.forInsert(201, "INITIAL", "NA", smsModuleName + appConfig.getOperator(), smsFeatureName, "", fileDto.getFileName(), LocalDateTime.now());
                    ModulesAuditTrail entity = modulesAuditTrailRepository.save(modulesAuditTrail);
                    moduleAuditId = entity.getId();

                    if(smsService.processSmsFile(fileDto, appConfig.getOperator().trim())) {
                        logger.error("Processing failed for file sms summary.");
                        logger.info("The summary for file {} is {}", fileDto.getFileName(), fileDto);
                        modulesAuditTrailRepository.updateModulesAudit(501, "FAIL", "Processing failed for sms summary", (int) fileDto.getTotalRecords(), (int) fileDto.getFailedRecords(), (int) (System.currentTimeMillis() - startTime), LocalDateTime.now(), (int) fileDto.getSuccessRecords(), moduleAuditId);
                        alertService.raiseAnAlert("alert5601", fileDto.getFileName(), appConfig.getOperator(), 0);
                        fileServiceUtils.moveFile(fileDto, appConfig.getMoveFilePath());
                        continue;
                    }
                    modulesAuditTrailRepository.updateModulesAudit(200, "SUCCESS", "NA", (int) fileDto.getTotalRecords(), (int) fileDto.getFailedRecords(), (int) (System.currentTimeMillis() - startTime), LocalDateTime.now(), (int) fileDto.getSuccessRecords(),moduleAuditId);
                    fileServiceUtils.moveFile(fileDto, appConfig.getMoveFilePath());
                    logger.info("The summary for file {} is {}", fileDto.getFileName(), fileDto);
                } catch (Exception e) {
                    logger.error("Processing failed for file sms summary due to error {}", e.getMessage());
                    logger.info("The summary for file {} is {}", fileDto.getFileName(), fileDto);
                    modulesAuditTrailRepository.updateModulesAudit(501, "FAIL", "Processing failed for sms summary due to error " + e.getMessage(), (int) fileDto.getTotalRecords(), (int) fileDto.getFailedRecords(), (int) (System.currentTimeMillis() - startTime), LocalDateTime.now(), (int) fileDto.getSuccessRecords(), moduleAuditId);
                    alertService.raiseAnAlert("alert5601", fileDto.getFileName(), appConfig.getOperator(), 0);
                    fileServiceUtils.moveFile(fileDto, appConfig.getMoveFilePath());
                }
            }
        } catch (Exception e) {
            logger.error("Processing failed for sms summary due to error {}",e.getMessage());
//            modulesAuditTrailRepository.updateModulesAudit(501, "FAIL", "TProcessing failed for ussd summary due to error " +e.getMessage(), (int)fileDto.getExceptionListSuccess() + (int)fileDto.getBlackListSuccess() + (int)fileDto.getGreyListSuccess(), (int) fileDto.getBlackListFailure() +  (int) fileDto.getGreyListFailure() + (int) fileDto.getExceptionListFailure() , (int) (System.currentTimeMillis() - startTime), LocalDateTime.now(), moduleAuditId);
            alertService.raiseAnAlert("alert5602", "", "", 0);
        }
    }
}
