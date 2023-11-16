package com.globalpbx.performansmanager.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.globalpbx.performansmanager.constant.HeadersConstants;
import com.globalpbx.performansmanager.dto.MailInfoDto;
import com.globalpbx.performansmanager.dto.SendDiskUsageDto;
import com.globalpbx.performansmanager.dto.SystemAlertDto;
import com.globalpbx.performansmanager.dto.SystemPerformansDto;
import com.globalpbx.performansmanager.feignclient.MailService;
import com.globalpbx.performansmanager.repository.DiskRepository;
import com.globalpbx.performansmanager.service.DiskService;
import com.globalpbx.performansmanager.util.FormatterUtil;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.globalpbx.performansmanager.util.FormatterUtil.formatFileSize;
import static com.globalpbx.performansmanager.util.FormatterUtil.generateHTMLTable;

@Service
public class DiskServiceImpl implements DiskService {

    @Value("${sqlite.database.url}")
    private String databaseUrl;

    @Value("${spring.performans-manager.path}")
    private String databasePath;

    @Value("${spring.performans-manager-system-alerts.path}")
    private String databaseSystemAlerts;

    private final DiskRepository diskRepository;

    private final MailService mailService;


    private List<SystemAlertDto> systemAlertDtoList;


    public DiskServiceImpl(DiskRepository diskRepository, MailService mailService) {
        this.diskRepository = diskRepository;
        this.mailService = mailService;
    }

    @Override
    @Cacheable("diskUsage")
    public List<String[]> getDiskUsageByPath(String path, int maxDepth) {
        List<String[]> fileSizeList = new ArrayList<>();
        File file = new File(path);

        if (file.exists()) {
            try {
                Files.walk(Path.of(path), maxDepth)
                        .parallel()
                        .forEach(subDir -> {
                            long subDirSize = 0;
                            try {
                                subDirSize = Files.walk(subDir)
                                        .filter(Files::isRegularFile)
                                        .parallel()
                                        .mapToLong(p -> p.toFile().length())
                                        .sum();
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                            fileSizeList.add(new String[]{String.valueOf(subDir.getFileName()), formatFileSize(subDirSize)});
                        });
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            return fileSizeList;
        }
        fileSizeList.add(new String[]{"file could not be found"});
        return fileSizeList;
    }

    @Override
    public SystemPerformansDto getSystemPerformans() {
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long diskUsagePercentage = 100 * (os.getTotalMemorySize() - os.getFreeMemorySize()) / os.getTotalMemorySize();
        double cpuUsagePercentage = 100 * os.getCpuLoad();
        long processCpuTime = os.getProcessCpuTime() / 1000000;

        File diskPartition = new File("C:");
        long totalCapacity = diskPartition.getTotalSpace();
        double freePartitionSpace = (double) diskPartition.getFreeSpace();
        double freeSpacePercentage = 100 * freePartitionSpace / totalCapacity;

        return SystemPerformansDto.builder()
                .diskUsagePercentage(FormatterUtil.formatWithDecimal(diskUsagePercentage))
                .cpuUsagePercentage(FormatterUtil.formatWithDecimal(cpuUsagePercentage))
                .processCpuTime(FormatterUtil.formatWithDecimal(processCpuTime))
                .freeSpacePercentage(FormatterUtil.formatWithDecimal(freeSpacePercentage))
                .build();
    }

    @Override
    public String createSystemAlert(SystemAlertDto systemAlertDto) throws ClassNotFoundException, SQLException {

        Connection connection;
        // SQLite JDBC driver has been created
        Class.forName(databaseUrl);

        // SQLite db connection has been created
        connection = DriverManager.getConnection(databaseSystemAlerts);

        System.out.println("You have successfully connected to the SQLite database.");

        String convertToString = String.join(", ", systemAlertDto.getEmailList());
        systemAlertDto.setEmailList(List.of(convertToString));

        diskRepository.createSystemAlertsTable(connection);
        diskRepository.saveSystemAlert(connection, systemAlertDto);
        systemAlertDtoList = diskRepository.getAllSystemAlert(connection);
        return "System alert has been added";
    }


    @Scheduled(fixedRate = 1000)
    public void systemAlertControlUnit() {
        if (systemAlertDtoList != null && !systemAlertDtoList.isEmpty()) {
            SystemPerformansDto systemPerformans = getSystemPerformans();
            System.out.println(systemPerformans);
            String[] headers = {HeadersConstants.DISK_USAGE_PERCENTAGE,
                    HeadersConstants.CPU_USAGE_PERCENTAGE,
                    HeadersConstants.PROCESS_CPU_TIME,
                    HeadersConstants.FREE_SPACE,
                    HeadersConstants.TIME_EXCEEDED};
            String[][] data = {
                    {systemPerformans.getDiskUsagePercentage(),
                            systemPerformans.getCpuUsagePercentage(),
                            systemPerformans.getProcessCpuTime(),
                            systemPerformans.getFreeSpacePercentage(),
                            String.valueOf(3)
                    }
            };
            systemAlertDtoList.stream().parallel().forEach(systemAlertDto -> {
                if (systemAlertDto.getLimitOfCpuUsagePercentage() < Double.parseDouble(systemPerformans.getCpuUsagePercentage())) {
                    if (systemAlertDto.getStartCPUOverloadTime() == 0) {
                        systemAlertDto.setStartCPUOverloadTime(System.currentTimeMillis());
                    } else if (System.currentTimeMillis() - systemAlertDto.getStartCPUOverloadTime() > 3 && !systemAlertDto.isMailSent()) {
                        String tableContent = generateHTMLTable(headers, data, HeadersConstants.CPU_USAGE_PERCENTAGE);
                        sendMailToEmailList(systemAlertDto, tableContent);
                        systemAlertDto.setMailSent(true);
                    }
                } else if (systemAlertDto.getStartCPUOverloadTime() != 0) {
                    System.out.println("CPU Overload Time -> " + (System.currentTimeMillis() - systemAlertDto.getStartCPUOverloadTime()));
                    systemAlertDto.setStartCPUOverloadTime(0);
                    System.out.println(systemAlertDto.getLimitOfCpuUsagePercentage());
                    systemAlertDto.setMailSent(false);
                }

                if (systemAlertDto.getLimitOfDiskUsagePercentage() < Double.parseDouble(systemPerformans.getDiskUsagePercentage())) {
                    if (systemAlertDto.getStartDiskOverloadTime() == 0) {
                        systemAlertDto.setStartDiskOverloadTime(System.currentTimeMillis());
                    } else if (System.currentTimeMillis() - systemAlertDto.getStartDiskOverloadTime() > 3 && !systemAlertDto.isMailSent()) {
                        String tableContent = generateHTMLTable(headers, data, HeadersConstants.DISK_USAGE_PERCENTAGE);
                        sendMailToEmailList(systemAlertDto, tableContent);
                        systemAlertDto.setMailSent(true);
                    }
                } else if (systemAlertDto.getStartDiskOverloadTime() != 0) {
                    System.out.println("Disk Overload Time -> " + (System.currentTimeMillis() - systemAlertDto.getStartDiskOverloadTime()));
                    systemAlertDto.setStartDiskOverloadTime(0);
                    System.out.println(systemAlertDto);
                    systemAlertDto.setMailSent(false);
                }

                if (systemAlertDto.getLimitOfProcessCpuTime() < Long.parseLong(systemPerformans.getProcessCpuTime())) {
                    if (systemAlertDto.getStartCPUTimeOverloadTime() == 0) {
                        systemAlertDto.setStartCPUTimeOverloadTime(System.currentTimeMillis());
                    } else if (System.currentTimeMillis() - systemAlertDto.getStartCPUTimeOverloadTime() > 3 && !systemAlertDto.isMailSent()) {
                        String tableContent = generateHTMLTable(headers, data, HeadersConstants.PROCESS_CPU_TIME);
                        sendMailToEmailList(systemAlertDto, tableContent);
                        systemAlertDto.setMailSent(true);
                    }
                } else if (systemAlertDto.getStartCPUTimeOverloadTime() != 0) {
                    System.out.println("CPU Time Overload Time -> " + (System.currentTimeMillis() - systemAlertDto.getStartCPUTimeOverloadTime()));
                    systemAlertDto.setStartCPUTimeOverloadTime(0);
                    System.out.println(systemAlertDto);
                    systemAlertDto.setMailSent(false);
                }

                if (systemAlertDto.getLimitOfFreeSpacePercentage() > Double.parseDouble(systemPerformans.getFreeSpacePercentage())) {
                    if (systemAlertDto.getStartFreeSpaceOverloadTime() == 0) {
                        systemAlertDto.setStartFreeSpaceOverloadTime(System.currentTimeMillis());
                    } else if (System.currentTimeMillis() - systemAlertDto.getStartFreeSpaceOverloadTime() > 3 && !systemAlertDto.isMailSent()) {
                        String tableContent = generateHTMLTable(headers, data, HeadersConstants.FREE_SPACE);
                        sendMailToEmailList(systemAlertDto, tableContent);
                        systemAlertDto.setMailSent(true);
                    }
                } else if (systemAlertDto.getStartFreeSpaceOverloadTime() != 0) {
                    System.out.println("Free Space Overload Time -> " + (System.currentTimeMillis() - systemAlertDto.getStartFreeSpaceOverloadTime()));
                    systemAlertDto.setStartFreeSpaceOverloadTime(0);
                    System.out.println(systemAlertDto);
                    systemAlertDto.setMailSent(false);
                }
            });
        }
    }

    private void sendMailToEmailList(SystemAlertDto systemAlertDto, String tableContent) {
        List<MailInfoDto> diskUsageMailInfoDtoList = new ArrayList<>();
        systemAlertDto.getEmailList().forEach((emailAddress) -> {
            MailInfoDto diskUsageMailInfoDto = MailInfoDto.builder()
                    .path(databasePath)
                    .versionNumber(1)
                    .recipient(emailAddress)
                    .subject(HeadersConstants.DISK_USAGE_WARNING)
                    .body(tableContent)
                    .isHtml(true)
                    .build();
            diskUsageMailInfoDtoList.add(diskUsageMailInfoDto);
        });
        mailService.sendMail(diskUsageMailInfoDtoList);
    }
}
