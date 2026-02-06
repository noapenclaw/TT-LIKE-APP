package com.ttlikeapp.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * CurrentUser Annotation
 * 
 * Use this annotation to inject the authenticated user into controller methods.
 * 
 * Example:
 *   @GetMapping("/me")
 *   public ResponseEntity<UserResponse> getCurrentUser(@CurrentUser UserPrincipal user) {
 *       // user contains authenticated user details
 *   }
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}
