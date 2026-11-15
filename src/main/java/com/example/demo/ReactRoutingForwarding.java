package com.example.demo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ReactRoutingForwarding {

    
    @RequestMapping({
        "/dashboard", 
        "/nueva-cita", 
        "/admin", 
        "/admin/**"
    })
    public String forwardToReact() {
        
        return "forward:/index.html";
    }
}