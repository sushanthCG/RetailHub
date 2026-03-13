package com.application.RetailHub.Services;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.application.RetailHub.Entities.User;
import com.application.RetailHub.Repositories.UserRepository;

@Service
public class UserService {

	  private final UserRepository userRepository;
	  private final BCryptPasswordEncoder passwordEncoder;

	  // Constructor Injection
	  public UserService(UserRepository userRepository)
	  {
	        this.userRepository = userRepository;
	        this.passwordEncoder = new BCryptPasswordEncoder();
	  }

	    // ================= REGISTER USER =================
	  public User UserRegister(User user)
	    {
	    	// Check username
	    	Optional<User> username = userRepository.findByUsername(user.getUsername());
	    	if(username.isPresent()) {
	    		throw new RuntimeException("user name already exists");
	    	}

	    	// Check email
	    	Optional<User> useremail = userRepository.findByEmail(user.getEmail());
	    	if(useremail.isPresent()) {
	    		throw new RuntimeException("user email already exists");
	    	}

	    	// Encode Password
	    	user.setPassword(passwordEncoder.encode(user.getPassword()));

	    	return userRepository.save(user);
	    }
}
