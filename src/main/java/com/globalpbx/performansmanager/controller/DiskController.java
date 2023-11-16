package com.globalpbx.performansmanager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.globalpbx.performansmanager.dto.DiskUsageDto;
import com.globalpbx.performansmanager.dto.SendDiskUsageDto;
import com.globalpbx.performansmanager.dto.SystemAlertDto;
import com.globalpbx.performansmanager.dto.SystemPerformansDto;
import com.globalpbx.performansmanager.enums.DiskUsageEnum;
import com.globalpbx.performansmanager.service.DiskService;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/disks")
public class DiskController {

    private DiskService diskService;

    public DiskController(DiskService diskService) {
        this.diskService = diskService;
    }

    @GetMapping
    public ResponseEntity<List<String[]>> getDiskUsage(@RequestBody DiskUsageDto diskUsageDto) throws IOException {
        return new ResponseEntity<>(diskService.getDiskUsageByPath(diskUsageDto.getPath(), DiskUsageEnum.ALL.getValue()), HttpStatus.OK);
    }

    @GetMapping("/details")
    public ResponseEntity<List<String[]>> getDiskUsageDetails(@RequestBody DiskUsageDto diskUsageDto) throws IOException {
        return new ResponseEntity<>(diskService.getDiskUsageByPath(diskUsageDto.getPath(), DiskUsageEnum.DETAIL.getValue()), HttpStatus.OK);
    }

    @GetMapping("/performans")
    public ResponseEntity<SystemPerformansDto> getSystemPerformans(){
        return new ResponseEntity<>(diskService.getSystemPerformans(),HttpStatus.OK);
    }

    @PostMapping("/alert")
    public ResponseEntity<String> createSystemAlert(@RequestBody SystemAlertDto systemAlertDto) throws SQLException, ClassNotFoundException {
        return new ResponseEntity<>(diskService.createSystemAlert(systemAlertDto),HttpStatus.CREATED);
    }
}
