package com.gl.eirs.ussd.repository.app;

import com.gl.eirs.ussd.entity.app.Ussd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UssdRepository extends JpaRepository<Ussd, Long> {

}
