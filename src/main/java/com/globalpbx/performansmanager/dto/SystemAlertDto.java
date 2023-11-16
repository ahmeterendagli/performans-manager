package com.globalpbx.performansmanager.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemAlertDto {
    private double limitOfDiskUsagePercentage;
    private double limitOfCpuUsagePercentage;
    private long limitOfProcessCpuTime;
    private double limitOfFreeSpacePercentage;
    private List<String> emailList;
    private LocalDateTime createdTime;
    private long startCPUOverloadTime = 0L;
    private long startDiskOverloadTime = 0L;
    private long startCPUTimeOverloadTime = 0L;
    private long startFreeSpaceOverloadTime = 0L;
    private boolean isMailSent = false;

    @Override
    public String toString() {
        return "SystemAlertDto{" +
                "limitOfDiskUsagePercentage=" + limitOfDiskUsagePercentage +
                ", limitOfCpuUsagePercentage=" + limitOfCpuUsagePercentage +
                ", limitOfProcessCpuTime=" + limitOfProcessCpuTime +
                ", limitOfFreeSpacePercentage=" + limitOfFreeSpacePercentage +
                ", emailList=" + emailList +
                ", createdTime=" + createdTime +
                ", startCPUOverloadTime=" + startCPUOverloadTime +
                ", startDiskOverloadTime=" + startDiskOverloadTime +
                ", startCPUTimeOverloadTime=" + startCPUTimeOverloadTime +
                ", startFreeSpaceOverloadTime=" + startFreeSpaceOverloadTime +
                '}';
    }
}
