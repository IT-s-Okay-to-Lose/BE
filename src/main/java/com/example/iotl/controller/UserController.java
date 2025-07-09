package com.example.iotl.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.iotl.dto.UserInfoDto;
import com.example.iotl.dto.security.CustomOAuth2User;
import com.example.iotl.service.security.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final CustomOAuth2UserService customOAuth2UserService;

	@GetMapping("/userinfo")
	public ResponseEntity<?> getUserInfo() {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			CustomOAuth2User userDetails = (CustomOAuth2User) authentication.getPrincipal();

			String username = userDetails.getUsername();
			UserInfoDto userInfo = customOAuth2UserService.getNameAndCreatedAt(username);
			return ResponseEntity.ok(userInfo);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(404).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
		}
	}
}