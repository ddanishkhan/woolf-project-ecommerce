package com.ecommerce.dto.response;

import java.util.List;

public record TokenAuthenticationResponse(
        boolean valid,
        List<String> authorities,
        String jti,
        String username
) {}

