package com.tw.medtech.pfa.web.dto;

import com.tw.medtech.pfa.model.enums.Role;

import java.util.List;

// `password` is required on create; on update, leave it null/blank to
// keep the user's existing password unchanged rather than requiring it
// re-entered just to edit a name or role.
public record UserRequest(String name, String email, String password, int phoneNumber, List<Role> roles) {}
