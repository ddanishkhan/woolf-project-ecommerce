package com.usermanagement.service;

import com.usermanagement.dto.AddressDTO;
import com.usermanagement.dto.CreateAddressDTO;
import com.usermanagement.model.Address;
import com.usermanagement.model.User;
import com.usermanagement.repository.AddressRepository;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public List<AddressDTO> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<AddressDTO> getAddressesByEmail(String email) {
        return addressRepository.findByUserEmail(email).stream()
                .map(this::convertToDto)
                .toList();
    }

    public AddressDTO addAddress(Long userId, CreateAddressDTO addressDTO) {
        Address address = convertToEntity(addressDTO);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        address.setUser(user);

        if (address.isDefault()) {
            // If the new address is set as default, unset default for all other addresses of this user
            List<Address> userAddresses = addressRepository.findByUserId(userId);
            userAddresses.forEach(addr -> {
                if (addr.isDefault()) {
                    addr.setDefault(false);
                    addressRepository.save(addr);
                }
            });
        }

        return convertToDto(addressRepository.save(address));
    }

    public AddressDTO updateAddress(Long addressId, AddressDTO updatedAddress) {

        Address existingAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + addressId));

        // If the updated address is set as default, unset default for all other addresses of this user
        if (updatedAddress.isDefault()) {
            List<Address> userAddresses = addressRepository.findByUserId(existingAddress.getUser().getId());
            userAddresses.forEach(addr -> {
                if (addr.isDefault() && !addr.getId().equals(addressId)) {
                    addr.setDefault(false);
                    addressRepository.save(addr);
                }
            });
        }

        existingAddress.setStreet(updatedAddress.getStreet());
        existingAddress.setCity(updatedAddress.getCity());
        existingAddress.setState(updatedAddress.getState());
        existingAddress.setZipCode(updatedAddress.getZipCode());
        existingAddress.setCountry(updatedAddress.getCountry());
        existingAddress.setDefault(updatedAddress.isDefault());

        return convertToDto(addressRepository.save(existingAddress));
    }

    public void deleteAddress(Long addressId) {
        addressRepository.deleteById(addressId);
    }

    public Optional<Address> getAddressById(Long addressId) {
        return addressRepository.findById(addressId);
    }

    private AddressDTO convertToDto(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .isDefault(address.isDefault())
                .build();
    }

    private Address convertToEntity(CreateAddressDTO addressDTO) {
        return Address.builder()
                .street(addressDTO.getStreet())
                .city(addressDTO.getCity())
                .state(addressDTO.getState())
                .zipCode(addressDTO.getZipCode())
                .country(addressDTO.getCountry())
                .isDefault(addressDTO.isDefault())
                .build();
    }

}