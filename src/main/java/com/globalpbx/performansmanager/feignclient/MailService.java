package com.globalpbx.performansmanager.feignclient;

import com.globalpbx.performansmanager.dto.MailInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(value = "mailService",url = "http://localhost:8080/api/")
public interface MailService {

    @PostMapping("mail/")
    String sendMail(List<MailInfoDto> mailInfoDtoList);
}
