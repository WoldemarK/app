package com.example.personservice.service;

import com.example.personservice.client.ClientApi;
import com.example.personservice.dto.UserDto;
import com.example.personservice.entity.Address;
import com.example.personservice.entity.Countries;
import com.example.personservice.entity.User;
import com.example.personservice.repository.AddressRepository;
import com.example.personservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ClientApi clientApi;

    @Transactional
    public User createUser(User user) {
        Countries countries = getCountries(user);
        Address address = getAddress(user, countries);
        User created = getUser(user, address);
        if (created != null) {
            log.info("User created: {}", created);
            userRepository.save(created);
        }
        UserDto userDto = UserDto.builder()
                .uuid(created.getId())
                .username(user.getFirstName())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .password(created.getSecretKey())
                .build();
        clientApi.create(userDto);

        return created;
    }

    @Transactional(readOnly = true)
    public User getUserInformation(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: {}" + userId));
    }

    private static Countries getCountries(User user) {
        return Countries.builder()
                .name(user.getAddressId().getCountryId().getName())
                .status(user.getAddressId().getCountryId().getStatus())
                .alpha2(user.getAddressId().getCountryId().getAlpha2())
                .alpha3(user.getAddressId().getCountryId().getAlpha3())
                .build();
    }

    private Address getAddress(User user, Countries countries) {
        return addressRepository.findByCityAndStateAndZipCode(user.getAddressId().getCity(),
                user.getAddressId().getZipCode(),
                user.getAddressId().getState()).orElseGet(() ->
                Address.builder()
                        .city(user.getAddressId()
                                .getCity())
                        .state(user.getAddressId()
                                .getState()).zipCode(user.getAddressId().getZipCode())
                        .address(user.getAddressId().getAddress())
                        .countryId(countries).build());
    }

    private static User getUser(User user, Address address) {
        return User.builder()
                .email(user.getEmail())
                .filled(user.isFilled())
                .lastName(user.getLastName())
                .firstName(user.getFirstName())
                .secretKey(user.getSecretKey())
                .addressId(address)
                .build();
    }
}
