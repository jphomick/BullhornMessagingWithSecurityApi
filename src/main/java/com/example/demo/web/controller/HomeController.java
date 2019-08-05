package com.example.demo.web.controller;

import com.cloudinary.utils.ObjectUtils;
import com.example.demo.business.entities.Message;
import com.example.demo.business.entities.User;
import com.example.demo.business.entities.repositories.MessageRepository;
import com.example.demo.business.entities.repositories.UserRepository;
import com.example.demo.business.services.CloudinaryConfig;
import com.example.demo.business.services.CustomerUserDetails;
import com.example.demo.business.services.UserService;
import com.example.demo.business.util.MD5Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Map;

@RestController
public class HomeController {
    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CloudinaryConfig cloudc;

    @Autowired
    UserService userService;

    @RequestMapping("/list")
    public @ResponseBody ArrayList<Message> listMessages() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Message> messages = new ArrayList<>();
        return messageRepository.findAllByOrderByPostedDateTimeDesc();
    }

    @PostMapping("/process")
    public @ResponseBody boolean processForm(Message message, @RequestParam("file") MultipartFile file) {

        message.setUser(userService.getUser());

        if (file != null && !file.isEmpty()) {
            try {
                Map uploadResult = cloudc.upload(
                        file.getBytes(), ObjectUtils.asMap("resourcetype", "auto"));
                String url = uploadResult.get("url").toString();
                String uploadedName = uploadResult.get("public_id").toString();
                String transformedImage = cloudc.createUrl(uploadedName, 150, 150);
                message.setPicturePath(transformedImage);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        messageRepository.save(message);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(message.getId()).toUri();

        return true;
    }

    @RequestMapping("/get_current")
    public @ResponseBody String follow() {
        try {
            return new ObjectMapper().writeValueAsString(userService.getUser());
        } catch (Exception e) {
            return "";
        }
    }

    @RequestMapping("/get_user_{id}")
    public @ResponseBody String getUser(@PathVariable("id") long id) {
        try {
            return new ObjectMapper().writeValueAsString(userRepository.findById(id).get());
        } catch (Exception e) {
            return "";
        }
    }

    @RequestMapping("/get_{id}")
    public @ResponseBody String showMessage(@PathVariable("id") long id) {
        try {
            return new ObjectMapper().writeValueAsString(messageRepository.findById(id).get());
        } catch (Exception e) {
            return "";
        }
    }

    @RequestMapping("/delete_{id}")
    public @ResponseBody String check(@PathVariable("id") long id) {
        messageRepository.deleteById(id);
        return "Deleted";
    }
}
