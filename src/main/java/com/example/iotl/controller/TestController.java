package com.example.iotl.controller;

import com.example.iotl.domain.TestEntity;
import com.example.iotl.repository.TestEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final TestEntityRepository repository;

    @PostMapping
    public String save(@RequestParam String name) {
        TestEntity t = new TestEntity();
        t.setName(name);
        repository.save(t);
        return "saved";
    }
}
