package com.usermanagement.controller;

import com.usermanagement.dto.AddressDTO;
import com.usermanagement.dto.CreateAddressDTO;
import com.usermanagement.model.Address;
import com.usermanagement.security.JwtTokenProvider;
import com.usermanagement.service.AddressService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/me")
    public ResponseEntity<List<AddressDTO>> getCurrentLoggedInUserAddresses(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) {
        String token = jwtTokenProvider.extractAndValidateToken(authorizationHeader);
        String email = jwtTokenProvider.getEmailFromToken(token);
        List<AddressDTO> addressDTOs = addressService.getAddressesByEmail(email);
        return ResponseEntity.ok(addressDTOs);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressDTO>> getAddressesByUserId(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long userId) {
        String token = jwtTokenProvider.extractAndValidateToken(authorizationHeader);
        if (!jwtTokenProvider.isAdmin(token)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<AddressDTO> addressDTOs = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(addressDTOs);
    }

    @PostMapping("/me")
    public ResponseEntity<AddressDTO> addAddress(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody CreateAddressDTO addressDTO) {
        String token = jwtTokenProvider.extractAndValidateToken(authorizationHeader);
        Long userId = Long.valueOf(jwtTokenProvider.getSubjectFromJWT(token));

        AddressDTO newAddress = addressService.addAddress(userId, addressDTO);
        return ResponseEntity.ok(newAddress);
    }

    @PutMapping("/{addressId}/me")
    public ResponseEntity<AddressDTO> updateAddress(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long addressId, @RequestBody AddressDTO addressDTO) {
        String token = jwtTokenProvider.extractAndValidateToken(authorizationHeader);
        Long userId = Long.valueOf(jwtTokenProvider.getSubjectFromJWT(token));

        Address existingAddress = addressService.getAddressById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + addressId));

        if (!existingAddress.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        AddressDTO updatedAddress = addressService.updateAddress(addressId, addressDTO);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/{addressId}/me")
    public ResponseEntity<Void> deleteAddress(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long addressId) {
        String token = jwtTokenProvider.extractAndValidateToken(authorizationHeader);
        Long userId = Long.valueOf(jwtTokenProvider.getSubjectFromJWT(token));

        Address existingAddress = addressService.getAddressById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + addressId));

        if (!existingAddress.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        addressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }

}