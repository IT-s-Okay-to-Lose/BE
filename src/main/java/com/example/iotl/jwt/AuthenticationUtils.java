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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationUtils {
	public static String getCurrentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			log.warn("🔴 SecurityContextHolder.getContext().getAuthentication() is null");
			return null;
		}

		log.info("🟢 Authentication object: {}", authentication);
		log.info("🟢 Principal: {}", authentication.getPrincipal());

		if (!authentication.isAuthenticated()) {
			log.warn("🔴 Authentication is not authenticated");
			return null;
		}

		Object principal = authentication.getPrincipal();

		if (principal instanceof CustomOAuth2User customUser) {
			log.info("✅ Principal is CustomOAuth2User: {}", customUser.getUsername());
			return customUser.getUsername();
		} else if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
			log.info("✅ Principal is UserDetails: {}", userDetails.getUsername());
			return userDetails.getUsername();
		} else {
			log.warn("⚠️ Principal is unknown type: {}", principal.getClass().getName());
			return principal.toString(); // fallback
		}
	}
}
