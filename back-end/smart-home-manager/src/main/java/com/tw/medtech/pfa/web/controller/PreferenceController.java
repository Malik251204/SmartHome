package com.tw.medtech.pfa.web.controller;

import com.tw.medtech.pfa.service.PreferenceService;
import com.tw.medtech.pfa.web.dto.PreferenceDto;
import com.tw.medtech.pfa.web.dto.PreferenceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;

    @GetMapping
    public List<PreferenceDto> getPreferencesForUser(@RequestParam Long userId) {
        return preferenceService.getPreferencesForUser(userId);
    }

    @PostMapping
    public PreferenceDto createPreference(@RequestBody PreferenceRequest request) {
        return preferenceService.createPreference(request);
    }

    @PutMapping("/{id}")
    public PreferenceDto updatePreference(@PathVariable Long id, @RequestBody PreferenceRequest request) {
        return preferenceService.updatePreference(id, request);
    }

    @DeleteMapping("/{id}")
    public void deletePreference(@PathVariable Long id) {
        preferenceService.deletePreference(id);
    }
}
