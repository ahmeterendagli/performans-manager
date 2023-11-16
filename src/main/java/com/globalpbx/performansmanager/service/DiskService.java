package com.globalpbx.performansmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.globalpbx.performansmanager.dto.SendDiskUsageDto;
import com.globalpbx.performansmanager.dto.SystemAlertDto;
import com.globalpbx.performansmanager.dto.SystemPerformansDto;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface DiskService {

    List<String[]> getDiskUsageByPath(String path,int maxDepth) throws IOException;

    SystemPerformansDto getSystemPerformans();

    String createSystemAlert(SystemAlertDto systemAlertDto) throws ClassNotFoundException, SQLException;
}
