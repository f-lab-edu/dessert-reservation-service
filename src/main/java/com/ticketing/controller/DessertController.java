package com.ticketing.controller;

import com.ticketing.common.controller.BaseController;
import com.ticketing.dto.DessertReq;
import com.ticketing.dto.DessertRes;
import com.ticketing.service.DessertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DessertController extends BaseController {
    private final DessertService dessertService;

    @PostMapping("/desserts")
    private ResponseEntity<DessertRes> createDessert(@Valid @ModelAttribute DessertReq dessertReq){
        return ResponseEntity.ok(dessertService.createDessert(dessertReq));
    }
}
