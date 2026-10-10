# StayPilot — Implementation Plan

## Phases

### Phase 1: Foundation Setup ✦ CURRENT
- [x] Initialize git repository
- [ ] Create Spring Boot backend with Maven
- [ ] Create React + Vite + TypeScript frontend
- [ ] Configure PostgreSQL + Flyway
- [ ] Docker Compose for local dev (Postgres)
- [ ] Basic CI configuration
- [ ] Verify everything starts and connects

### Phase 2: Auth & Multi-Tenancy
- [ ] User entity + Flyway migration
- [ ] Organization entity + membership
- [ ] JWT authentication (login, refresh, logout)
- [ ] Registration flow
- [ ] Role/permission model
- [ ] Tenant context derivation
- [ ] Auth API endpoints
- [ ] Frontend login/register pages
- [ ] Protected routes
- [ ] Tenant isolation tests

### Phase 3: Property & Occupancy
- [ ] Property CRUD + API
- [ ] Room CRUD + API
- [ ] Bed management + status model
- [ ] Resident registration
- [ ] Booking workflow
- [ ] Occupancy assignment
- [ ] Bed availability view
- [ ] Transfer workflow
- [ ] Frontend property/room/bed/resident pages
- [ ] Concurrency tests for bed allocation

### Phase 4: Financial Workflows
- [ ] Rent rate configuration
- [ ] Charge generation logic
- [ ] Payment recording
- [ ] Payment allocation
- [ ] Outstanding balance calculation
- [ ] Deposit ledger
- [ ] Receipts
- [ ] Move-out settlement
- [ ] Financial calculation tests
- [ ] Frontend billing/payment pages

### Phase 5: Maintenance & Reports
- [ ] Maintenance ticket CRUD
- [ ] Ticket status workflow
- [ ] Expense management
- [ ] Dashboard metrics (real data)
- [ ] Occupancy reports
- [ ] Collection reports
- [ ] Frontend dashboard + reports

### Phase 6: Portal & Notifications
- [ ] Resident portal (read-only views)
- [ ] In-app notifications
- [ ] Email notification (optional)
- [ ] Notification preferences

### Phase 7: Subscription & Admin
- [ ] Subscription plan model
- [ ] Entitlement checks
- [ ] Platform admin interface
- [ ] Organization management

### Phase 8: Production Hardening
- [ ] Security audit
- [ ] Rate limiting
- [ ] CORS hardening
- [ ] Dockerfile (backend)
- [ ] Frontend production build
- [ ] CI/CD pipeline
- [ ] Backup/restore documentation
- [ ] Landing page

## First Vertical Slice Target
Create org → Login → Create property → Add room + beds → Register resident → Assign bed → View occupancy
