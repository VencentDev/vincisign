package com.vencentdev.backend.modules.user.enums;

/**
 * The highest identity assurance level explicitly established for an account.
 *
 * <p>Levels are not inferred from one another. Each level must be backed by its own evidence and
 * verification timestamp when a later ticket adds verification workflows.
 */
public enum IdentityLevel {
  I0,
  I1,
  I2,
  I3,
  I4,
  I5,
  I6
}
