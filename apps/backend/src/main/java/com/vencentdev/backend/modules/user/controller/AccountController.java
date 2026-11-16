package com.vencentdev.backend.modules.user.controller;

import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.auth.CurrentUser;
import com.vencentdev.backend.modules.user.dto.UserResponse;
import com.vencentdev.backend.modules.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

  private final UserService users;

  public AccountController(UserService users) {
    this.users = users;
  }

  @GetMapping("/me")
  public UserResponse me(@CurrentUser AuthenticatedUser user) {
    return users.getMe(user);
  }
}
