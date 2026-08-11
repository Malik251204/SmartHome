package com.tw.medtech.pfa.web.dto;

import com.tw.medtech.pfa.model.enums.Role;

import java.util.List;

public record UserRequest(String name, String email, int phoneNumber, List<Role> roles) {}
