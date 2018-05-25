package com.vistorieja.emailws;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailWsController {

    @GetMapping
    public String index(){

        return "hello World";

    }
}
