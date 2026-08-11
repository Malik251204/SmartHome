package com.tw.medtech.pfa.service;

import com.tw.medtech.pfa.web.dto.PreferenceDto;
import com.tw.medtech.pfa.web.dto.PreferenceRequest;

import java.util.List;

public interface PreferenceService {
    List<PreferenceDto> getPreferencesForUser(Long userId);
    PreferenceDto createPreference(PreferenceRequest request);
    PreferenceDto updatePreference(Long id, PreferenceRequest request);
    void deletePreference(Long id);
}
