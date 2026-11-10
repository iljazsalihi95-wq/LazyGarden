# LazyGarden gameplay backend (staging)
This branch adds an initial D1 gameplay API alongside the existing AI routes. **Not production-ready and not deployed.**
Endpoints:
- POST /api/game/guest -> guest session token (store securely)
- GET /api/game/state -> persisted player state
- POST /api/game/attempt/start {level} -> attempt id, deterministic seed and level limits
- POST /api/game/attempt/submit {attempt_id,moves:[]} -> **pending_verification**, never awards currency
- GET /api/game/health -> database binding status

Required Cloudflare steps (not performed):
1. Create D1 database `lazygarden-db` in Cloudflare dashboard.
2. Add `[[d1_databases]]` binding `DB` with the actual database_id to wrangler.toml (never invent the id).
3. Apply `cloudflare-worker/migrations/0001_game.sql` to staging D1.
4. Test with Wrangler and automated API tests; deploy staging before production.

Security work required before production: server-side deterministic replay of Match-3 moves; robust auth linking AppCreator24/Android identity; rate limiting, CORS allowlist, life regeneration and atomic start transactions; purchases must be verified server-side; WET Wallet integration requires verified existing Apps Script contract and cannot credit unverified scores. No real-money purchases are enabled.
