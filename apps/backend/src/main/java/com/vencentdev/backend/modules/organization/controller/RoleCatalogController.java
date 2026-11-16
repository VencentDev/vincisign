package com.vencentdev.backend.modules.organization.controller;

import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.auth.CurrentUser;
import com.vencentdev.backend.modules.organization.dto.RolePermissionResponse;
import com.vencentdev.backend.modules.organization.service.RolePermissionCatalog;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleCatalogController {

  @GetMapping
  public List<RolePermissionResponse> list(@CurrentUser AuthenticatedUser user) {
    return RolePermissionCatalog.all();
  }
}
