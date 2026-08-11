package com.tw.medtech.pfa.service.impl;

import com.tw.medtech.pfa.dao.repository.PreferenceRepository;
import com.tw.medtech.pfa.dao.repository.RoomRepository;
import com.tw.medtech.pfa.dao.repository.UserRepository;
import com.tw.medtech.pfa.exception.ResourceNotFoundException;
import com.tw.medtech.pfa.model.Preference;
import com.tw.medtech.pfa.model.Room;
import com.tw.medtech.pfa.model.User;
import com.tw.medtech.pfa.service.PreferenceService;
import com.tw.medtech.pfa.web.dto.PreferenceDto;
import com.tw.medtech.pfa.web.dto.PreferenceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// No ownership/authorization check here (e.g. "only the author can
// edit") — nothing else in this backend enforces authorization at all
// (no Spring Security anywhere), so adding a bespoke rule just for this
// resource would be inconsistent, not safer. That's left to the
// frontend, same as admin-only screens already are.
@Service
@RequiredArgsConstructor
public class PreferenceServiceImpl implements PreferenceService {

    private final PreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PreferenceDto> getPreferencesForUser(Long userId) {
        return preferenceRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PreferenceDto createPreference(PreferenceRequest request) {
        User user = findUserOrThrow(request.userId());
        Room room = request.roomId() != null ? findRoomOrThrow(request.roomId()) : null;

        Preference preference = Preference.builder()
                .user(user)
                .room(room)
                .text(request.text())
                .enabled(request.enabled())
                .build();

        return toDto(preferenceRepository.save(preference));
    }

    @Override
    @Transactional
    public PreferenceDto updatePreference(Long id, PreferenceRequest request) {
        Preference preference = findPreferenceOrThrow(id);
        Room room = request.roomId() != null ? findRoomOrThrow(request.roomId()) : null;

        preference.setRoom(room);
        preference.setText(request.text());
        preference.setEnabled(request.enabled());

        return toDto(preferenceRepository.save(preference));
    }

    @Override
    @Transactional
    public void deletePreference(Long id) {
        preferenceRepository.delete(findPreferenceOrThrow(id));
    }

    private Preference findPreferenceOrThrow(Long id) {
        return preferenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preference not found with id: " + id));
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private Room findRoomOrThrow(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    }

    private PreferenceDto toDto(Preference p) {
        return new PreferenceDto(
                p.getId(),
                p.getUser().getId(),
                p.getRoom() != null ? p.getRoom().getId() : null,
                p.getRoom() != null ? p.getRoom().getName() : null,
                p.getText(),
                p.isEnabled(),
                p.getCreatedAt()
        );
    }
}
