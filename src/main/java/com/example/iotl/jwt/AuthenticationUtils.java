package com.example.iotl.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// public class AuthenticationUtils {
// 	public static String getCurrentUsername() {
// 		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
// 		if (authentication == null || !authentication.isAuthenticated()) return null;
//
// 		Object principal = authentication.getPrincipal();
// 		if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
// 			return userDetails.getUsername();
// 		} else {
// 			return principal.toString();
// 		}
// 	}
// }
import com.example.iotl.dto.security.CustomOAuth2User;

public class AuthenticationUtils {
	public static String getCurrentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) return null;

		Object principal = authentication.getPrincipal();

		if (principal instanceof CustomOAuth2User customUser) {
			return customUser.getUsername();
		} else if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
			return userDetails.getUsername();
		} else {
			return principal.toString(); // fallback
		}
	}
}