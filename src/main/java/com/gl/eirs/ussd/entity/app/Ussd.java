package com.gl.eirs.ussd.entity.app;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
@Data
@Table(name="ussd_usage_1205")
public class Ussd {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="request_init_date")
    String requestInitDate;

    @Column(name="request_type")
    String requestType;

    @Column(name="ussd_session_id")
    String ussdSessionId;

    @Column(name="msisdn")
    String msisdn;

    @Column(name="imsi")
    String imsi;

    @Column(name="input")
    String input;

    @Column(name="session_end_date_time")
    String sessionEndDateTime;

    @Column(name="reason_session_close")
    String reasonSessionClose;

    @Column(name="response_lang")
    String respondLanguage;

    @Column(name="operator")
    String operator;

    @Column(name="file_name")
    String fileName;


}
