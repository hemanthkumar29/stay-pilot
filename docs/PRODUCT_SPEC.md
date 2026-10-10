# StayPilot — Product Specification

## Product Overview

**StayPilot** is a multi-tenant SaaS platform for managing Paying Guest (PG) accommodations, hostels, and co-living spaces. It targets small-to-medium operators (20–150 beds) in India who currently rely on notebooks, spreadsheets, or fragmented tools.

## Core Value Proposition

"Manage your PG operations from one place — from resident move-in to rent collection, maintenance, and move-out."

## Target Users

| Persona | Role | Key Capabilities |
|---------|------|-------------------|
| Platform Admin | StayPilot operator | Manage tenants, subscriptions, platform health |
| Property Owner | Paying customer | Full property, financial, and team management |
| Caretaker | Property employee | Day-to-day operations within granted permissions |
| Resident | Occupant | View stay, payments, raise maintenance tickets |

## MVP Scope (v1.0)

### Module Priority

1. **Auth & Account Management** — Registration, login, sessions, roles
2. **Multi-Tenant Architecture** — Organization isolation, membership
3. **Property Management** — CRUD, configuration, archival
4. **Room & Bed Inventory** — Room/bed entities, status model, availability
5. **Booking & Resident Onboarding** — Reservations, move-in, bed assignment
6. **Resident Lifecycle** — Transfers, move-out, history preservation
7. **Rent & Charge Management** — Charge generation, billing cycles, ledger
8. **Payment Management** — Recording verified payments, allocations, receipts
9. **Security Deposit Management** — Separate deposit ledger, settlement
10. **Maintenance Tickets** — Create, assign, track, resolve
11. **Expense Management** — Record, categorize, filter
12. **Dashboard & Reporting** — Real metrics from database
13. **Resident Portal** — Read-only stay info, tickets, move-out requests
14. **Notifications** — In-app, extensible to email
15. **Subscription Management** — Plans, entitlements, manual activation
16. **Platform Administration** — Org management, audit, health

## Business Rules & Assumptions

### Occupancy
- A room contains one or more beds; beds are independently assignable
- Bed statuses: AVAILABLE, RESERVED, OCCUPIED, MAINTENANCE_BLOCKED, INACTIVE
- Only one active occupancy per bed at any time (enforced by DB constraint)
- Historical occupancy records are preserved indefinitely

### Billing
- Charges and payments are separate entities linked via allocations
- Rent rates are snapshotted at charge-generation time
- Previously generated charges are immutable to rate changes
- All monetary values use BigDecimal with INR (₹), 2 decimal places
- Charge generation is idempotent per billing period

### Deposits
- Security deposits are tracked in a separate ledger
- Deposits are not treated as revenue
- Settlement occurs at move-out with documented deductions

### Multi-Tenancy
- Each organization is a tenant; data is isolated at application + DB level
- Server derives tenant context from authenticated user; never trusts client-supplied tenant IDs
- Cross-tenant access is tested and prevented

### Subscriptions
- Manual activation for v1; payment provider integration is a future milestone
- Expired subscriptions restrict access but never delete data

## Pricing Hypotheses (Configurable)

| Plan | Price | Properties | Beds | Staff |
|------|-------|------------|------|-------|
| Starter | ₹799/mo | 1 | 50 | 3 |
| Growth | ₹1,499/mo | 3 | 150 | 10 |
| Professional | ₹2,499/mo | 10 | 500 | 25 |

These are hypotheses to validate with customers, not final prices.
