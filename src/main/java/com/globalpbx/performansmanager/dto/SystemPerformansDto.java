package com.globalpbx.performansmanager.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemPerformansDto {
    private String diskUsagePercentage;
    private String cpuUsagePercentage;
    private String processCpuTime;
    private String freeSpacePercentage;

    @Override
    public String toString() {
        return "SystemPerformansDto{" +
                "diskUsagePercentage='" + diskUsagePercentage + '\'' +
                ", cpuUsagePercentage='" + cpuUsagePercentage + '\'' +
                ", processCpuTime='" + processCpuTime + '\'' +
                ", freeSpacePercentage='" + freeSpacePercentage + '\'' +
                '}';
    }
}
