package com.example.personservice.service;

import com.example.personservice.client.ClientApi;
import com.example.personservice.dto.UserDto;
import com.example.personservice.entity.Address;
import com.example.personservice.entity.Countries;
import com.example.personservice.entity.User;
import com.example.personservice.repository.AddressRepository;
import com.example.personservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ClientApi clientApi;

    private UserService userService;

    private User inputUser;
    private Address inputAddress;
    private Countries inputCountry;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, addressRepository, clientApi);
        inputCountry = Countries.builder()
                .name("Test Country")
                .alpha2("TC")
                .alpha3("TST")
                .status("ACTIVE")
                .build();
        inputAddress = Address.builder()
                .city("Test City")
                .state("Test State")
                .zipCode("12345")
                .address("Test Address")
                .countryId(inputCountry)
                .build();
        inputUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .filled(false)
                .secretKey("secret123")
                .addressId(inputAddress)
                .build();
    }



    @Test
    void createUser_ExistingAddress_ShouldUseExistingAddress() {
        Address existingAddress = Address.builder()
                .city("Test City")
                .state("Test State")
                .zipCode("12345")
                .address("Test Address")
                .countryId(inputCountry)
                .build();

        User expectedUser = User.builder()
                .email(inputUser.getEmail())
                .filled(inputUser.isFilled())
                .lastName(inputUser.getLastName())
                .firstName(inputUser.getFirstName())
                .secretKey(inputUser.getSecretKey())
                .addressId(existingAddress)
                .build();

        when(addressRepository.findByCityAndStateAndZipCode(
                inputUser.getAddressId().getCity(),
                inputUser.getAddressId().getZipCode(),
                inputUser.getAddressId().getState()))
                .thenReturn(Optional.of(existingAddress));

        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        User result = userService.createUser(inputUser);

        assertNotNull(result);
        assertEquals(existingAddress, result.getAddressId());

        verify(addressRepository).findByCityAndStateAndZipCode(
                inputUser.getAddressId().getCity(),
                inputUser.getAddressId().getZipCode(),
                inputUser.getAddressId().getState());
        verify(addressRepository, never()).save(any(Address.class));
        verify(userRepository).save(any(User.class));
        verify(clientApi).create(any(UserDto.class));
    }



    @Test
    void getUserInformation_UserExists_ShouldReturnUser() {
        UUID userId = UUID.randomUUID();
        User expectedUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        User result = userService.getUserInformation(userId);

        assertEquals(expectedUser, result);
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserInformation_UserNotExists_ShouldThrowException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserInformation(userId));
        verify(userRepository).findById(userId);
    }



    @Test
    void createUser_NullAddressInInputUser_ShouldHandleGracefully() {
        User userWithoutAddress = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        assertThrows(NullPointerException.class, () -> userService.createUser(userWithoutAddress));
    }
}