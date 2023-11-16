package com.globalpbx.performansmanager.repository;

import com.globalpbx.performansmanager.dto.SystemAlertDto;
import com.globalpbx.performansmanager.dto.SystemPerformansDto;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Repository
public interface DiskRepository {

    void createSystemAlertsTable(Connection connection);
    SystemAlertDto saveSystemAlert(Connection connection, SystemAlertDto systemAlertDto) throws SQLException;

    List<SystemAlertDto> getAllSystemAlert(Connection connection);

}
