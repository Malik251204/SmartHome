package com.tw.medtech.pfa.web.dto;

// No `roles` field — self-registration always creates a plain USER.
// Promoting someone to ADMIN happens afterward, through PUT /api/users/{id}
// by an existing admin.
public record RegisterRequest(String name, String email, String password, int phoneNumber) {}
