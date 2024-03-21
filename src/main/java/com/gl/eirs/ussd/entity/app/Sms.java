package com.gl.eirs.ussd.entity.app;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="sms_summary")
public class Sms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="msisdn")
    String msisdn;

    @Column(name="sms_received_date")
    LocalDateTime smsReceivedDate;

    @Column(name="short_code")
    String shortCode;

    @Column(name="request_from_customer")
    String requestFromCustomer;

    @Column(name="response_date_time")
    LocalDateTime responseDateTime;

    @Column(name="response")
    String response;

    @Column(name="request_lang")
    String requestLang;

    @Column(name="response_lang")
    String responseLang;

    @Column(name="operator")
    String operator;

    @Column(name="file_name")
    String fileName;
}
