# User management service
Handles user registration, login, and authorization, JWT token validations and generation.

## Project Overview

This user management service is a core component for handling user-related functionalities within the e-commerce project. It provides comprehensive features for user authentication and authorization, including user registration, login, and role management. The service supports both traditional username/password authentication and OAuth2 integration with platforms like Google and Facebook.

Key functionalities include:
*   **User Authentication & Authorization**: Secure login, registration, and JWT-based token validation, including token blacklisting for logout and session management.
*   **Profile Management**: Users can manage their personal profiles, while administrators have capabilities to view and manage all user profiles.
*   **Address Management**: Users can securely store and manage multiple addresses, with administrative oversight for user addresses.
*   **Password Management**: Features for forgotten password recovery and resetting.
*   **Internal Service Authentication**: Provides a mechanism for internal services to securely authenticate and interact with the user management system using dedicated service tokens.

