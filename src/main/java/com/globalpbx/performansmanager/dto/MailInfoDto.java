package com.globalpbx.performansmanager.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailInfoDto {
    private long id;
    private String path;
    private float versionNumber;
    private String recipient;
    private String subject;
    private String body;
    private LocalDateTime sendTime;
    private Boolean isHtml;

    @Override
    public String toString() {
        return "MailInfoDto{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", versionNumber=" + versionNumber +
                ", recipient='" + recipient + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                ", sendTime=" + sendTime +
                '}';
    }
}
