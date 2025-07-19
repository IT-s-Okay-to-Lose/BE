package com.example.iotl.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.iotl.dto.UserDto;
import com.example.iotl.global.response.BaseResponse;
import com.example.iotl.repository.TestEntityRepository;

import lombok.RequiredArgsConstructor;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/test")
public class TestControllerAuth {

	@GetMapping
	public ResponseEntity<BaseResponse<?>> test() {

		UserDto userDto = new UserDto();
		userDto.setUsername("Naver 132123r4234234234");
		userDto.setRole("admin");
		userDto.setName("admin-greentea");
		userDto.setProfileImageUrl("admin-greentea.jpg");

		BaseResponse<Object> response = BaseResponse.builder()
			.isSuccess(true)
			.message("성공")
			.code(200)
			.data(userDto)
			.build();

		return ResponseEntity.ok(response);
	}



	@GetMapping("/getUsername")
	public String authTest1() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// 로그인되지 않은 경우
		if (authentication == null || !authentication.isAuthenticated()) {
			return "Unauthorized";
		}

		// username 추출 (principal은 기본적으로 UserDetails 혹은 문자열 username)
		Object principal = authentication.getPrincipal();
		String username;

		if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
			username = userDetails.getUsername();
		} else {
			username = principal.toString(); // 일반적으로는 그냥 username
		}

		return "Current username: " + username;
	}
}
