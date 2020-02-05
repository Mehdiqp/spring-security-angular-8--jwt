package com.backend.springsecurity.controllers;

import com.backend.springsecurity.model.ERole;
import com.backend.springsecurity.model.Role;
import com.backend.springsecurity.model.User;
import com.backend.springsecurity.payload.request.LoginResuest;
import com.backend.springsecurity.payload.request.SignupRequest;
import com.backend.springsecurity.payload.response.JwtResponse;
import com.backend.springsecurity.payload.response.MessageRespones;
import com.backend.springsecurity.repository.RoleRepository;
import com.backend.springsecurity.repository.UserRepository;
import com.backend.springsecurity.service.securityService.JwtUtils;
import com.backend.springsecurity.service.securityService.UserDetailsImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Autowired//inject our dependency
    public AuthController(UserRepository userRepository, RoleRepository roleRepository, JwtUtils jwtUtils, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    //user login controller
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginResuest loginResuest) {

        //update spring authentication with loginRequest
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginResuest.getUsername(), loginResuest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        //generated token from auth
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImp userDetailsImp = (UserDetailsImp) authentication.getPrincipal();

        List<String> roles = userDetailsImp.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        //response to user
        return ResponseEntity.ok(new JwtResponse(jwt, userDetailsImp.getId(), userDetailsImp.getUsername(),
                userDetailsImp.getEmail(), roles));

    }

    @PostMapping("/signup") //create user controller
    public ResponseEntity<?> regiseterUser(@Valid @RequestBody SignupRequest signupRequest) {
        //check username to be uniq
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageRespones("ERROR:user is already exist"));
        }

        //check email be uniq
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageRespones("ERROR : Email is already exist"));
        }

        //create user object
        User user = new User(
                signupRequest.getUsername(), signupRequest.getEmail(), passwordEncoder.encode(signupRequest.getPassword()));

        Set<String> strRoles = signupRequest.getRole();
        Set<Role> userRoles = new HashSet<>();

        //check user role and fetch from db
        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            userRoles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        userRoles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        userRoles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        userRoles.add(userRole);
                }
            });
        }
            user.setRoles(userRoles);
            userRepository.save(user);
            return ResponseEntity.ok("successfully!!!");
    }

}
