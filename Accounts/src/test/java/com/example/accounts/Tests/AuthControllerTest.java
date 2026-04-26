package com.example.accounts.Tests;
import com.example.accounts.Configurations.JwtUtil;
import com.example.accounts.Controller.AuthController;
import com.example.accounts.Entity.Account;
import com.example.accounts.Entity.AccountDTO;
import com.example.accounts.Entity.User;
import com.example.accounts.Entity.UserDTO;
import com.example.accounts.Repository.AccountRepository;
import com.example.accounts.Repository.UserRepository;
import com.example.accounts.Service.AccountService;
import kafka.Kafka;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @Mock
        private AuthenticationManager myManager;
    @Mock
        private AccountRepository accRepo;
    @Mock
        private UserRepository userRepo;
    @Mock
        private JwtUtil jwtUtil;
    @Mock
        private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    @Test
    public void testRegister_Succes(){
        UserDTO newUser = new UserDTO("lau_user","parola123","Lau",25,"RO1234",new BigDecimal("100.0"));

        when(passwordEncoder.encode("parola123")).thenReturn("parola_criptata");

        ResponseEntity<String> reponse = authController.register(newUser);

        assertEquals(HttpStatus.CREATED,reponse.getStatusCode());
        assertEquals("Succes",reponse.getBody());
        verify(accRepo, times(1)).save(any(Account.class));
        verify(userRepo, times(1)).save(any(User.class));
    }
    @Test
    public void testRegister_Fails(){
        UserDTO newUser = new UserDTO("lau_user","parola123","Lau",25,"RO1234",new BigDecimal("100.0"));

        when(accRepo.save(any(Account.class))).thenThrow(new RuntimeException("Poc!"));

        ResponseEntity<String> reponse = authController.register(newUser);

        assertEquals(HttpStatus.BAD_REQUEST,reponse.getStatusCode());
        verify(userRepo,never()).save(any(User.class));
    }
    @Test
    public void testLogin_Succes(){
        User myUser = new User();
        myUser.setUsername("lau_user");
        myUser.setPassword("parola123");

        when(jwtUtil.generateToken("lau_user")).thenReturn("mocked-jwt-token");
        //myManager.authenticate() nu arunca erroare by default in mock
        ResponseEntity<String> response = authController.login(myUser);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals("mocked-jwt-token",response.getBody());
        verify(myManager,times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
    @Test
    public void testLogin_Fails(){
        User myUser = new User();
        myUser.setUsername("lau_user");
        myUser.setPassword("parola_gresita");

        when(myManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).
                thenThrow(new BadCredentialsException("Parola gresita!"));

        ResponseEntity<String> response = authController.login(myUser);

        assertEquals(HttpStatus.UNAUTHORIZED,response.getStatusCode());
        assertEquals("Credentiale gresite!",response.getBody());

        verify(jwtUtil,never()).generateToken(anyString());
    }

}
