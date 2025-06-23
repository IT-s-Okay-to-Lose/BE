package com.example.iotl.service;

import com.example.iotl.dto.OAuth2Response;
import com.example.iotl.dto.UserDto;
import com.example.iotl.entity.User;
import com.example.iotl.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDto saveOrUpdateOAuthUser(OAuth2Response oAuth2Response) {
        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        User user = userRepository.findByUsername(username);

        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setName(oAuth2Response.getName());
            user.setEmail(oAuth2Response.getEmail());
            user.setRole("ROLE_USER");
        } else {
            user.setName(oAuth2Response.getName());
            user.setEmail(oAuth2Response.getEmail());
        }

        userRepository.save(user);

        UserDto dto = new UserDto();
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setRole(user.getRole());

        return dto;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
