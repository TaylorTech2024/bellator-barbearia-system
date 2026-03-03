package com.taylortech.bellator.service;

import com.taylortech.bellator.dto.AuthLoginRequest;
import com.taylortech.bellator.dto.AuthRegisterRequest;
import com.taylortech.bellator.dto.AuthResponse;
import com.taylortech.bellator.dto.UsuarioResponse;
import com.taylortech.bellator.exception.ApiException;
import com.taylortech.bellator.model.Usuario;
import com.taylortech.bellator.repository.UsuarioRepository;
import com.taylortech.bellator.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepo, PasswordEncoder encoder,
                       AuthenticationManager authManager, JwtService jwtService) {
        this.usuarioRepo = usuarioRepo;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(AuthRegisterRequest req) {
        if (usuarioRepo.existsByEmail(req.email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email já cadastrado");
        }
        Usuario u = new Usuario();
        u.setNome(req.nome);
        u.setEmail(req.email.toLowerCase());
        u.setSenhaHash(encoder.encode(req.senha));
        u.setRole(req.role);
        u = usuarioRepo.save(u);

        String token = jwtService.generate(u.getEmail(), Map.of(
                "role", u.getRole().name(),
                "uid", u.getId()
        ));

        return new AuthResponse(token, u.getRole(), new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getRole()));
    }

    public AuthResponse login(AuthLoginRequest req) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email.toLowerCase(), req.senha));
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        Usuario u = usuarioRepo.findByEmail(req.email.toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        String token = jwtService.generate(u.getEmail(), Map.of(
                "role", u.getRole().name(),
                "uid", u.getId()
        ));

        return new AuthResponse(token, u.getRole(), new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getRole()));
    }
}
