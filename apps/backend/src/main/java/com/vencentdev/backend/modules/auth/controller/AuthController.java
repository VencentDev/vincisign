package com.vencentdev.backend.modules.auth.controller;

import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.auth.CurrentUser;
import com.vencentdev.backend.modules.auth.dto.AuthTokenResponse;
import com.vencentdev.backend.modules.auth.dto.ManualLoginRequest;
import com.vencentdev.backend.modules.auth.dto.ManualSignupRequest;
import com.vencentdev.backend.modules.auth.service.ManualAuthService;
import com.vencentdev.backend.modules.user.dto.UserResponse;
import com.vencentdev.backend.modules.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final UserService users;
  private final ManualAuthService manualAuth;

  public AuthController(UserService users, ManualAuthService manualAuth) {
    this.users = users;
    this.manualAuth = manualAuth;
  }

  @PostMapping("/signup")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthTokenResponse signup(@Valid @RequestBody ManualSignupRequest request) {
    return manualAuth.signup(request);
  }

  @PostMapping("/login")
  public AuthTokenResponse login(@Valid @RequestBody ManualLoginRequest request) {
    return manualAuth.login(request);
  }

  @GetMapping("/me")
  public UserResponse me(@CurrentUser AuthenticatedUser user) {
    return users.findOrProvision(user);
  }
}
