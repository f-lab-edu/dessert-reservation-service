package com.ticketing.common.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BaseController {

}
