package org.rundellse.squashleague.service;

import org.rundellse.squashleague.persistence.UserH2Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserH2Dao userH2Dao;



}
