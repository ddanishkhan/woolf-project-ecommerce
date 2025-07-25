# Cart Service
This service manages the ephemeral, session-based shopping carts for users. It is optimized for high-speed read/write operations.

- Adding, removing, and updating items in a user's cart.
- Retrieving the current state of a cart.
- Carts are often temporary and are cleared after placing an order. 
- Uses redis cache for fast cart retrieval.

## Redis hosted on 
https://console.upstash.com/login

