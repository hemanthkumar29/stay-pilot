# StayPilot — Architecture Overview

## System Architecture

StayPilot is a **modular monolith** with a React SPA frontend and Spring Boot backend.

```
┌─────────────────────────────────────────────┐
│              React SPA (Vite)               │
│  TypeScript · TailwindCSS · shadcn/ui       │
│  React Router · TanStack Query · Zod        │
└──────────────────┬──────────────────────────┘
                   │ HTTPS / REST
┌──────────────────▼──────────────────────────┐
│           Spring Boot Backend               │
│  Java 21 · Spring Security · Spring Data JPA│
│  Flyway · Bean Validation · OpenAPI         │
├─────────────────────────────────────────────┤
│  Modules:                                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐    │
│  │   Auth   │ │  Tenant  │ │ Property │    │
│  └──────────┘ └──────────┘ └──────────┘    │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐    │
│  │   Room   │ │   Bed    │ │ Resident │    │
│  └──────────┘ └──────────┘ └──────────┘    │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐    │
│  │ Booking  │ │ Billing  │ │ Payment  │    │
│  └──────────┘ └──────────┘ └──────────┘    │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐    │
│  │ Deposit  │ │  Maint.  │ │ Expense  │    │
│  └──────────┘ └──────────┘ └──────────┘    │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐    │
│  │ Report   │ │  Notify  │ │ Subscr.  │    │
│  └──────────┘ └──────────┘ └──────────┘    │
└──────────────────┬──────────────────────────┘
                   │ JDBC
┌──────────────────▼──────────────────────────┐
│             PostgreSQL 16                   │
│  Flyway migrations · Constraints · Indexes  │
│  Tenant-scoped queries · UTC timestamps     │
└─────────────────────────────────────────────┘
```

## Key Design Decisions

### Authentication
- JWT-based with short-lived access tokens (15 min) and refresh tokens (7 days)
- Access token in memory; refresh token in HttpOnly Secure cookie
- Token refresh endpoint for seamless session extension
- Refresh token rotation on use; old tokens revoked

### Multi-Tenancy
- Application-level tenant isolation via `organization_id` foreign keys
- Server derives tenant from authenticated user's membership
- All repository queries filter by organization
- Cross-tenant access tests in CI

### Package Structure (Backend)
```
com.staypilot/
├── common/          # Shared utilities, base entities, exceptions
├── config/          # App config, security config, OpenAPI
├── security/        # JWT, auth filters, user details
├── tenant/          # Tenant context, tenant-aware base
├── organization/    # Org CRUD, membership
├── user/            # User entity, profile
├── property/        # Property management
├── room/            # Room management
├── bed/             # Bed management, availability
├── resident/        # Resident profiles, lifecycle
├── booking/         # Reservations, confirmations
├── occupancy/       # Bed assignments, transfers
├── billing/         # Charges, rates, cycles
├── payment/         # Payment recording, allocations
├── deposit/         # Deposit ledger
├── maintenance/     # Tickets, assignments
├── expense/         # Expense tracking
├── reporting/       # Aggregation queries, reports
├── notification/    # In-app, email notifications
├── subscription/    # Plans, entitlements
└── audit/           # Audit logging
```

### Database
- PostgreSQL 16 with Flyway migrations
- UUIDs for primary keys
- All monetary columns: `NUMERIC(15,2)`
- All timestamps: `TIMESTAMPTZ` stored in UTC
- Soft-delete for entities with financial history

### API Design
- REST under `/api/v1`
- Consistent error responses with code, message, field errors
- Pagination via `page`, `size`, `sort` query params
- OpenAPI documentation auto-generated
