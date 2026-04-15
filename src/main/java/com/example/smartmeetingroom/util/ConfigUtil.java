package com.example.smartmeetingroom.util;

import com.example.smartmeetingroom.repository.AppConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;


@Component
public class ConfigUtil {

    private static AppConfigRepository appConfigRepository;

    @Autowired
    public void setAppConfigRepository(AppConfigRepository repository) {
        ConfigUtil.appConfigRepository = repository;
    }

    public static  Set<String> getAllowedValues(String key) {
        var allowedValues = appConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Config not found: " + key));
        return new HashSet<>(allowedValues.getConfigValue());
    }
}
