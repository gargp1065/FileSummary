package com.gl.eirs.ussd.repository.app;


import com.gl.eirs.ussd.entity.app.Sms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmsRepository extends  JpaRepository<Sms, Long>, JpaSpecificationExecutor<Sms> {


}
