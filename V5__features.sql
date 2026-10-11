-- PawConnect · V3: medications, appointments, vet messages, notifications, admin flag,
-- NGO campaigns and impact reports.

alter table public.profiles add column if not exists is_admin boolean not null default false;

create table if not exists public.medication_records (
  id uuid primary key default gen_random_uuid(),
  dog_id uuid not null references public.dogs(id) on delete cascade,
  medication_name text not null check (char_length(medication_name) between 1 and 120),
  dosage text check (char_length(dosage) <= 120),
  schedule text check (char_length(schedule) <= 200),
  start_date date,
  end_date date,
  created_at timestamptz not null default now(),
  check (start_date is null or end_date is null or end_date >= start_date)
);

create index if not exists medication_dog_idx on public.medication_records (dog_id);

create table if not exists public.appointments (
  id uuid primary key default gen_random_uuid(),
  dog_id uuid not null references public.dogs(id) on delete cascade,
  pet_parent_id uuid not null references public.profiles(id) on delete cascade,
  vet_id uuid not null references public.profiles(id) on delete cascade,
  scheduled_at timestamptz not null,
  status text not null default 'REQUESTED'
    check (status in ('REQUESTED', 'CONFIRMED', 'DECLINED', 'COMPLETED', 'CANCELLED')),
  reason text check (char_length(reason) <= 500),
  vet_notes text check (char_length(vet_notes) <= 2000),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists appointments_vet_idx on public.appointments (vet_id, scheduled_at);
create index if not exists appointments_owner_idx on public.appointments (pet_parent_id, scheduled_at);

create table if not exists public.vet_messages (
  id uuid primary key default gen_random_uuid(),
  appointment_id uuid not null references public.appointments(id) on delete cascade,
  sender_id uuid not null references public.profiles(id) on delete cascade,
  message_text text not null check (char_length(message_text) between 1 and 2000),
  sent_at timestamptz not null default now()
);

create index if not exists vet_messages_thread_idx on public.vet_messages (appointment_id, sent_at);

create table if not exists public.notifications (
  id uuid primary key default gen_random_uuid(),
  profile_id uuid not null references public.profiles(id) on delete cascade,
  title text not null check (char_length(title) <= 160),
  body text not null check (char_length(body) <= 1000),
  type text not null,
  is_read boolean not null default false,
  dedupe_key text unique,
  created_at timestamptz not null default now()
);

create index if not exists notifications_inbox_idx on public.notifications (profile_id, created_at desc);

create table if not exists public.ngo_campaigns (
  id uuid primary key default gen_random_uuid(),
  ngo_id uuid not null references public.profiles(id) on delete cascade,
  title text not null check (char_length(title) between 3 and 160),
  description text check (char_length(description) <= 2000),
  target_amount numeric(12,2) check (target_amount is null or target_amount > 0),
  start_date date,
  end_date date,
  status text not null default 'ACTIVE' check (status in ('ACTIVE', 'CLOSED')),
  created_at timestamptz not null default now(),
  check (start_date is null or end_date is null or end_date >= start_date)
);

create index if not exists campaigns_ngo_idx on public.ngo_campaigns (ngo_id);
create index if not exists campaigns_status_idx on public.ngo_campaigns (status);

create table if not exists public.impact_reports (
  id uuid primary key default gen_random_uuid(),
  campaign_id uuid not null references public.ngo_campaigns(id) on delete cascade,
  ngo_id uuid not null references public.profiles(id) on delete cascade,
  report_text text not null check (char_length(report_text) between 1 and 4000),
  dogs_fed_count int check (dogs_fed_count is null or dogs_fed_count >= 0),
  report_date date not null default current_date,
  created_at timestamptz not null default now()
);

create index if not exists impact_campaign_idx on public.impact_reports (campaign_id, report_date desc);
