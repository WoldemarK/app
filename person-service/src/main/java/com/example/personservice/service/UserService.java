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
import org.slf4j.MDC;
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
        try {
            MDC.put("userId", String.valueOf(user.getId()));
            MDC.put("firstName", user.getFirstName());
            MDC.put("lastName", user.getLastName());
            MDC.put("email", user.getEmail());
            MDC.put("filed", String.valueOf(user.isFilled()));
            log.info("Received user");

            Countries countries = getCountries(user);
            MDC.put("id", String.valueOf(countries.getId()));
            MDC.put("name", countries.getName());
            MDC.put("alpha2", countries.getAlpha2());
            MDC.put("alpha3", countries.getAlpha3());
            MDC.put("status", countries.getStatus());
            log.info("Received countries");

            Address address = getAddress(user, countries);
            MDC.put("city", address.getCity());
            MDC.put("state", address.getState());
            MDC.put("zipCode", address.getZipCode());
            MDC.put("address", address.getAddress());
            log.info("Received address");

            User created = getUser(user, address);

            if (created != null) {
                log.info("User created: {}", created);
                userRepository.save(created);
            }
            clientApi.create
                    (
                            UserDto.builder()
                                    .uuid(created.getId())
                                    .username(user.getFirstName())
                                    .firstName(user.getFirstName())
                                    .lastName(user.getLastName())
                                    .email(user.getEmail())
                                    .password(created.getSecretKey())
                                    .filled(user.isFilled())
                                    .build()
                    );
            log.info(user.toString());
            return created;
        } finally {
            MDC.clear();
        }
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
