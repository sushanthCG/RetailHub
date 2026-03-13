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

	  public UserService(UserRepository userRepository)
	  {
	        this.userRepository = userRepository;
	        this.passwordEncoder = new BCryptPasswordEncoder();
	  }

	  public User UserRegister(User user)
	    {
	    	
	    	Optional<User> username = userRepository.findByUsername(user.getUsername());
	    	if(username.isPresent()) {
	    		throw new RuntimeException("user name already exists");
	    	}

	    	
	    	Optional<User> useremail = userRepository.findByEmail(user.getEmail());
	    	if(useremail.isPresent()) {
	    		throw new RuntimeException("user email already exists");
	    	}

	    	
	    	user.setPassword(passwordEncoder.encode(user.getPassword()));

	    	return userRepository.save(user);
	    }
}
