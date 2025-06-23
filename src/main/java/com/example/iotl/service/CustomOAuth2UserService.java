package com.example.iotl.service;

import com.example.iotl.dto.*;
import com.example.iotl.entity.User;
import com.example.iotl.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
//@Service
//public class CustomOAuth2UserService extends DefaultOAuth2UserService {
//
//    private final UserRepository userRepository;
//
//    public CustomOAuth2UserService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        OAuth2User oAuth2User = super.loadUser(userRequest);
//
//        log.info("userRequest==================>" + userRequest);
//        log.info("oAuth2User===================>" + oAuth2User);
//
//        String registrationId = userRequest.getClientRegistration().getRegistrationId();
//        OAuth2Response oAuth2Response = null;
//
//        if(registrationId.equals("naver")) {
//            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
//        } else if(registrationId.equals("google")) {
//            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
//        } else if(registrationId.equals("kakao")) {
//            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
//        } else {
//            return null;
//        }
//
//
//        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
//        log.info("username ===>" + username);
//        User existData = userRepository.findByUsername(username);
//
//        if(existData == null) {
//            User userEntity = new User();
//            userEntity.setUsername(username);
//            userEntity.setName(oAuth2Response.getName());
//            userEntity.setEmail(oAuth2Response.getEmail());
//            userEntity.setRole("ROLE_USER");
//
//            userRepository.save(userEntity);
//
//            UserDto userDTO = new UserDto();
//            userDTO.setUsername(username);
//            userDTO.setName(oAuth2Response.getName());
//            userDTO.setRole("ROLE_USER");
//
//            return new CustomOAuth2User(userDTO);
//        } else {
//            existData.setEmail(oAuth2Response.getEmail());
//            existData.setName(oAuth2Response.getName());
//
//            userRepository.save(existData);
//
//            UserDto userDTO = new UserDto();
//            userDTO.setUsername(existData.getUsername());
//            userDTO.setName(oAuth2Response.getName());
//            userDTO.setRole(existData.getRole());
//
//            return new CustomOAuth2User(userDTO);
//        }
//    }
//
//    public UserInfoDto getNameAndCreatedAt(String username) {
//        UserInfoDto userInfoDto = new UserInfoDto();
//        User user = userRepository.findByUsername(username);
//
//        userInfoDto.setName(user.getName());
//        userInfoDto.setCreatedAt(user.getCreatedAt());
//
//        return userInfoDto;
//    }
//}

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    public CustomOAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response;

        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }

        UserDto userDto = userService.saveOrUpdateOAuthUser(oAuth2Response);
        return new CustomOAuth2User(userDto);
    }

    public UserInfoDto getNameAndCreatedAt(String username) {
        User user = userService.findByUsername(username);
        UserInfoDto dto = new UserInfoDto();
        dto.setName(user.getName());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}

