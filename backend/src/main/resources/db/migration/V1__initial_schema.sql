-- V1__initial_schema.sql
-- Core tables for StayPilot multi-tenant SaaS

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- ORGANIZATIONS (tenants)
-- ============================================================
CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    -- ACTIVE, SUSPENDED, CANCELLED
    phone VARCHAR(20),
    email VARCHAR(255),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(50) DEFAULT 'India',
    pincode VARCHAR(10),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_organizations_slug ON organizations(slug);
CREATE INDEX idx_organizations_status ON organizations(status);

-- ============================================================
-- USERS
-- ============================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    phone VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    -- ACTIVE, INACTIVE, LOCKED
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);

-- ============================================================
-- MEMBERSHIPS (user <-> org with role)
-- ============================================================
CREATE TABLE memberships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    role VARCHAR(30) NOT NULL DEFAULT 'OWNER',
    -- PLATFORM_ADMIN, OWNER, MANAGER, CARETAKER, RESIDENT
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    -- ACTIVE, INACTIVE, INVITED
    invited_at TIMESTAMPTZ,
    joined_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, organization_id)
);

CREATE INDEX idx_memberships_user ON memberships(user_id);
CREATE INDEX idx_memberships_org ON memberships(organization_id);
CREATE INDEX idx_memberships_role ON memberships(role);

-- ============================================================
-- REFRESH TOKENS
-- ============================================================
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash);

-- ============================================================
-- PROPERTIES
-- ============================================================
CREATE TABLE properties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    name VARCHAR(255) NOT NULL,
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10),
    phone VARCHAR(20),
    email VARCHAR(255),
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Kolkata',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    -- ACTIVE, INACTIVE, ARCHIVED
    rent_due_day INTEGER DEFAULT 1 CHECK (rent_due_day >= 1 AND rent_due_day <= 28),
    grace_period_days INTEGER DEFAULT 5 CHECK (grace_period_days >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_properties_org ON properties(organization_id);
CREATE INDEX idx_properties_status ON properties(status);

-- ============================================================
-- ROOMS
-- ============================================================
CREATE TABLE rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id UUID NOT NULL REFERENCES properties(id),
    room_number VARCHAR(50) NOT NULL,
    floor INTEGER DEFAULT 0,
    room_type VARCHAR(30) NOT NULL DEFAULT 'STANDARD',
    -- STANDARD, DELUXE, PREMIUM
    sharing_type VARCHAR(20) NOT NULL DEFAULT 'SINGLE',
    -- SINGLE, DOUBLE, TRIPLE, QUAD, DORMITORY
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    -- ACTIVE, INACTIVE, MAINTENANCE
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(property_id, room_number)
);

CREATE INDEX idx_rooms_property ON rooms(property_id);
CREATE INDEX idx_rooms_status ON rooms(status);

-- ============================================================
-- BEDS
-- ============================================================
CREATE TABLE beds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL REFERENCES rooms(id),
    bed_label VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    -- AVAILABLE, RESERVED, OCCUPIED, MAINTENANCE_BLOCKED, INACTIVE
    monthly_rent NUMERIC(15,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(room_id, bed_label)
);

CREATE INDEX idx_beds_room ON beds(room_id);
CREATE INDEX idx_beds_status ON beds(status);

-- ============================================================
-- RESIDENTS
-- ============================================================
CREATE TABLE residents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(20) NOT NULL,
    emergency_contact_name VARCHAR(200),
    emergency_contact_phone VARCHAR(20),
    id_proof_type VARCHAR(50),
    id_proof_number VARCHAR(100),
    permanent_address TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    -- ACTIVE, MOVED_OUT, CANCELLED
    user_id UUID REFERENCES users(id),
    -- linked user account for portal access
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_residents_org ON residents(organization_id);
CREATE INDEX idx_residents_status ON residents(status);
CREATE INDEX idx_residents_phone ON residents(phone);
CREATE INDEX idx_residents_user ON residents(user_id);

-- ============================================================
-- BOOKINGS
-- ============================================================
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    property_id UUID NOT NULL REFERENCES properties(id),
    bed_id UUID REFERENCES beds(id),
    resident_id UUID NOT NULL REFERENCES residents(id),
    status VARCHAR(20) NOT NULL DEFAULT 'TENTATIVE',
    -- TENTATIVE, CONFIRMED, CANCELLED, EXPIRED, CONVERTED
    agreed_rent NUMERIC(15,2) NOT NULL,
    deposit_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    expected_move_in DATE NOT NULL,
    expected_move_out DATE,
    notes TEXT,
    expires_at TIMESTAMPTZ,
    confirmed_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bookings_org ON bookings(organization_id);
CREATE INDEX idx_bookings_property ON bookings(property_id);
CREATE INDEX idx_bookings_resident ON bookings(resident_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_bed ON bookings(bed_id);

-- ============================================================
-- OCCUPANCY ASSIGNMENTS
-- ============================================================
CREATE TABLE occupancy_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    resident_id UUID NOT NULL REFERENCES residents(id),
    bed_id UUID NOT NULL REFERENCES beds(id),
    booking_id UUID REFERENCES bookings(id),
    agreed_rent NUMERIC(15,2) NOT NULL,
    move_in_date DATE NOT NULL,
    expected_move_out_date DATE,
    actual_move_out_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    -- ACTIVE, MOVED_OUT, TRANSFERRED
    move_out_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Only one active occupancy per bed at any time
CREATE UNIQUE INDEX idx_occupancy_active_bed
    ON occupancy_assignments(bed_id) WHERE status = 'ACTIVE';

CREATE INDEX idx_occupancy_org ON occupancy_assignments(organization_id);
CREATE INDEX idx_occupancy_resident ON occupancy_assignments(resident_id);
CREATE INDEX idx_occupancy_bed ON occupancy_assignments(bed_id);
CREATE INDEX idx_occupancy_status ON occupancy_assignments(status);

-- ============================================================
-- RENT CHARGES
-- ============================================================
CREATE TABLE rent_charges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    occupancy_id UUID NOT NULL REFERENCES occupancy_assignments(id),
    resident_id UUID NOT NULL REFERENCES residents(id),
    charge_type VARCHAR(30) NOT NULL DEFAULT 'RENT',
    -- RENT, ADDITIONAL, ADJUSTMENT
    amount NUMERIC(15,2) NOT NULL,
    billing_period_start DATE NOT NULL,
    billing_period_end DATE NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- PENDING, PARTIALLY_PAID, PAID, OVERDUE, WAIVED
    rent_rate_at_generation NUMERIC(15,2) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(occupancy_id, billing_period_start, billing_period_end, charge_type)
);

CREATE INDEX idx_rent_charges_org ON rent_charges(organization_id);
CREATE INDEX idx_rent_charges_occupancy ON rent_charges(occupancy_id);
CREATE INDEX idx_rent_charges_resident ON rent_charges(resident_id);
CREATE INDEX idx_rent_charges_status ON rent_charges(status);
CREATE INDEX idx_rent_charges_due_date ON rent_charges(due_date);

-- ============================================================
-- PAYMENTS
-- ============================================================
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    resident_id UUID NOT NULL REFERENCES residents(id),
    amount NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    payment_method VARCHAR(30) NOT NULL,
    -- UPI, BANK_TRANSFER, CASH, OTHER
    payment_date DATE NOT NULL,
    transaction_reference VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'VERIFIED',
    -- VERIFIED, REVERSED, PENDING_VERIFICATION
    notes TEXT,
    recorded_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_org ON payments(organization_id);
CREATE INDEX idx_payments_resident ON payments(resident_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_date ON payments(payment_date);

-- ============================================================
-- PAYMENT ALLOCATIONS
-- ============================================================
CREATE TABLE payment_allocations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL REFERENCES payments(id),
    charge_id UUID NOT NULL REFERENCES rent_charges(id),
    amount NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_allocations_payment ON payment_allocations(payment_id);
CREATE INDEX idx_allocations_charge ON payment_allocations(charge_id);

-- ============================================================
-- DEPOSIT TRANSACTIONS
-- ============================================================
CREATE TABLE deposit_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    resident_id UUID NOT NULL REFERENCES residents(id),
    occupancy_id UUID REFERENCES occupancy_assignments(id),
    transaction_type VARCHAR(30) NOT NULL,
    -- DEPOSIT_RECEIVED, DEDUCTION, REFUND, ADJUSTMENT
    amount NUMERIC(15,2) NOT NULL,
    reason TEXT,
    payment_id UUID REFERENCES payments(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_deposit_tx_org ON deposit_transactions(organization_id);
CREATE INDEX idx_deposit_tx_resident ON deposit_transactions(resident_id);

-- ============================================================
-- MAINTENANCE TICKETS
-- ============================================================
CREATE TABLE maintenance_tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    property_id UUID NOT NULL REFERENCES properties(id),
    room_id UUID REFERENCES rooms(id),
    bed_id UUID REFERENCES beds(id),
    reported_by UUID REFERENCES users(id),
    resident_id UUID REFERENCES residents(id),
    category VARCHAR(50) NOT NULL,
    -- PLUMBING, ELECTRICAL, CLEANING, WIFI, FURNITURE, OTHER
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    -- LOW, MEDIUM, HIGH, URGENT
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    -- OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED
    assigned_to UUID REFERENCES users(id),
    resolved_at TIMESTAMPTZ,
    resolution_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tickets_org ON maintenance_tickets(organization_id);
CREATE INDEX idx_tickets_property ON maintenance_tickets(property_id);
CREATE INDEX idx_tickets_status ON maintenance_tickets(status);
CREATE INDEX idx_tickets_assigned ON maintenance_tickets(assigned_to);

-- ============================================================
-- MAINTENANCE COMMENTS
-- ============================================================
CREATE TABLE maintenance_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID NOT NULL REFERENCES maintenance_tickets(id),
    user_id UUID NOT NULL REFERENCES users(id),
    comment TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ticket_comments ON maintenance_comments(ticket_id);

-- ============================================================
-- EXPENSES
-- ============================================================
CREATE TABLE expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    property_id UUID NOT NULL REFERENCES properties(id),
    category VARCHAR(50) NOT NULL,
    -- ELECTRICITY, WATER, INTERNET, CLEANING, MAINTENANCE, REPAIRS, SUPPLIES, STAFF, OTHER
    amount NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    expense_date DATE NOT NULL,
    description TEXT,
    vendor VARCHAR(255),
    recorded_by UUID REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'RECORDED',
    -- RECORDED, APPROVED, REJECTED
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_expenses_org ON expenses(organization_id);
CREATE INDEX idx_expenses_property ON expenses(property_id);
CREATE INDEX idx_expenses_category ON expenses(category);
CREATE INDEX idx_expenses_date ON expenses(expense_date);

-- ============================================================
-- SUBSCRIPTION PLANS
-- ============================================================
CREATE TABLE subscription_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    monthly_price NUMERIC(15,2) NOT NULL,
    max_properties INTEGER NOT NULL DEFAULT 1,
    max_beds INTEGER NOT NULL DEFAULT 50,
    max_staff INTEGER NOT NULL DEFAULT 3,
    features JSONB DEFAULT '{}',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- SUBSCRIPTIONS
-- ============================================================
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    plan_id UUID NOT NULL REFERENCES subscription_plans(id),
    status VARCHAR(20) NOT NULL DEFAULT 'TRIAL',
    -- TRIAL, ACTIVE, EXPIRED, CANCELLED, SUSPENDED
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ,
    trial_ends_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscriptions_org ON subscriptions(organization_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);

-- ============================================================
-- AUDIT LOG
-- ============================================================
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID REFERENCES organizations(id),
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id UUID,
    details JSONB,
    ip_address VARCHAR(45),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_org ON audit_logs(organization_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);

-- ============================================================
-- SEED: Default subscription plans
-- ============================================================
INSERT INTO subscription_plans (name, code, monthly_price, max_properties, max_beds, max_staff, features) VALUES
('Starter', 'STARTER', 799.00, 1, 50, 3, '{"reports": true, "maintenance": true}'),
('Growth', 'GROWTH', 1499.00, 3, 150, 10, '{"reports": true, "maintenance": true, "deposits": true, "expenses": true}'),
('Professional', 'PROFESSIONAL', 2499.00, 10, 500, 25, '{"reports": true, "maintenance": true, "deposits": true, "expenses": true, "notifications": true, "csv_import": true}');
