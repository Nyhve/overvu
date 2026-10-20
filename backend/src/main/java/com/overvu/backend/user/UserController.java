package com.overvu.backend.user;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/preferences")
    public UserPreferencesResponse getPreferences() {
        return userService.getPreferences();
    }

    @PutMapping("/preferences")
    public UserPreferencesResponse updatePreferences(
            @RequestBody UpdateUserPreferencesRequest request) {
        return userService.updatePreferences(request);
    }
}