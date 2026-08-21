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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// Ownership enforced here, not in SecurityConfig — a URL-level rule can't
// express "only the author," since that depends on which preference, not
// just the endpoint. Editing is author-only; deleting is author or admin.
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
        requireAuthor(preference);

        Room room = request.roomId() != null ? findRoomOrThrow(request.roomId()) : null;

        preference.setRoom(room);
        preference.setText(request.text());
        preference.setEnabled(request.enabled());

        return toDto(preferenceRepository.save(preference));
    }

    @Override
    @Transactional
    public void deletePreference(Long id) {
        Preference preference = findPreferenceOrThrow(id);
        requireAuthorOrAdmin(preference);
        preferenceRepository.delete(preference);
    }

    // Reads the currently authenticated user's email from the security
    // context (set by JwtAuthenticationFilter) and looks up their real
    // User row — same lookup UserDetailsServiceImpl already does at login.
    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No user for authenticated email: " + email));
    }

    private void requireAuthor(Preference preference) {
        if (!preference.getUser().getId().equals(currentUser().getId())) {
            throw new AccessDeniedException("Only the author can edit this preference.");
        }
    }

    private void requireAuthorOrAdmin(Preference preference) {
        User current = currentUser();
        boolean isAuthor = preference.getUser().getId().equals(current.getId());
        boolean isAdmin = current.getRoles().stream().anyMatch(r -> r.name().equals("ADMIN"));
        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("Only the author or an admin can delete this preference.");
        }
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
