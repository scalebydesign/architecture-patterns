package com.scalebydesign.clean.interface_adapter.controller;

import com.scalebydesign.clean.entity.User;
import com.scalebydesign.clean.interface_adapter.presenter.UserResponse;
import com.scalebydesign.clean.usecase.GetUserUseCase;
import com.scalebydesign.clean.usecase.LoginUserUseCase;
import com.scalebydesign.clean.usecase.RegisterUserUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * INTERFACE ADAPTER LAYER (third circle in Clean Architecture)
 * 
 * Converts data from the format most convenient for the use cases/entities
 * to the format most convenient for external agents (web, DB, etc.)
 * 
 * In Clean Architecture, controllers belong here — they adapt HTTP to use case calls.
 * They also transform use case output into a response format (via Presenters).
 */
@RestController
@RequestMapping("/api/clean/users")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final GetUserUseCase getUserUseCase;

    public UserController(RegisterUserUseCase registerUserUseCase,
                          LoginUserUseCase loginUserUseCase,
                          GetUserUseCase getUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.getUserUseCase = getUserUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        User user = registerUserUseCase.execute(request.username(), request.email(), request.password());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        User user = loginUserUseCase.execute(request.username(), request.password());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        User user = getUserUseCase.execute(id);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    record RegisterRequest(String username, String email, String password) {}
    record LoginRequest(String username, String password) {}
}
