package com.example.demo.business.entities.repositories;

import com.example.demo.business.entities.Message;
import com.example.demo.business.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

public interface MessageRepository extends CrudRepository<Message, Long> {
    ArrayList<Message> findAllByUser(User user);

    ArrayList<Message> findAllByOrderByPostedDateTimeDesc();
}
