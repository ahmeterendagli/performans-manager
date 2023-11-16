package com.globalpbx.performansmanager.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiskUsageDto {
    private String path;

    @Override
    public String toString() {
        return "DiskUsageDto{" +
                "path='" + path + '\'' +
                '}';
    }
}
