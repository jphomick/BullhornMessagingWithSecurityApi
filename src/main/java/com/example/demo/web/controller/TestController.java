package com.example.demo.web.controller;

import com.cloudinary.utils.ObjectUtils;
import com.example.demo.business.entities.Message;
import com.example.demo.business.entities.User;
import com.example.demo.business.entities.repositories.MessageRepository;
import com.example.demo.business.entities.repositories.UserRepository;
import com.example.demo.business.services.CloudinaryConfig;
import com.example.demo.business.services.UserService;
import com.example.demo.business.util.MD5Util;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Map;

@Controller
public class TestController {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CloudinaryConfig cloudc;

    @Autowired
    UserService userService;

    @GetMapping("/add")
    public String messageForm(Principal principal, Model model) {
        model.addAttribute("imageLabel", "Upload Image");
        if (userService.getUser() == null) {
            model.addAttribute("user", new User());
        } else {
            model.addAttribute("user", userService.getUser());
        }
        model.addAttribute("message", new Message());
        return "messageform";
    }

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        model.addAttribute("page_title", "New User Registration");
        model.addAttribute("user", new User());
        return "register";
    }

    @RequestMapping("/")
    public String listMessages(Model model) {
        model.addAttribute("messages", messageRepository.findAllByOrderByPostedDateTimeDesc());//generate select * statement
        //we need because the below statement wont run if there is no authenticate user
        if (userService.getUser() != null) {
            model.addAttribute("user", userService.getUser());
//            model.addAttribute("HASH", MD5Util.md5Hex(userService.getUser().getEmail()));
        }
        model.addAttribute("mD5Util", new MD5Util());
        return "list";
    }

    @PostMapping("/processResult")
    public String processForm(Message message, @RequestParam("file") MultipartFile file) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = createHeaders("dave", "password");

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Boolean> response = restTemplate.exchange("http://localhost:8080/process", HttpMethod.POST,
                request, Boolean.class, message, file);
        boolean result = response.getBody();

        if (result) {
            return "redirect:/list";
        }
        return "redirect:/add";
    }

    HttpHeaders createHeaders(String username, String password){
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")) );
            String authHeader = "Basic " + new String( encodedAuth );
            set( "Authorization", authHeader );
        }};
    }
}
