-- V4__seed_data.sql
INSERT INTO public.coin_earning_rules (activity_type, coins_awarded, description, is_active)
VALUES
    ('VACCINATION_LOG', 50, 'Awarded when a verified vaccination record is added', true),
    ('SYMPTOM_LOG', 15, 'Awarded when logging a preliminary symptom check for your dog', true),
    ('HEALTH_CHECKUP', 40, 'Awarded upon completion of a vet consultation', true),
    ('STORE_PURCHASE', 20, 'Awarded per 500 INR spent on PawStore dog wellness products', true),
    ('EVENT_ATTENDANCE', 30, 'Awarded when attending an NGO adoption or feeding drive', true),
    ('PROFILE_COMPLETION', 25, 'Awarded for completing pet parent and dog details', true)
ON CONFLICT (activity_type) DO UPDATE SET
    coins_awarded = EXCLUDED.coins_awarded,
    description = EXCLUDED.description;

INSERT INTO public.product_categories (name, slug, description, icon_name)
VALUES
    ('Food & Nutrition', 'food-nutrition', 'Premium dog food, nutritious treats, and vital supplements', 'Bone'),
    ('Healthcare & Meds', 'healthcare-meds', 'Dewormers, tick & flea treatments, and first-aid kits', 'HeartPulse'),
    ('Grooming & Hygiene', 'grooming-hygiene', 'Organic shampoos, dental chews, and paw balms', 'Sparkles'),
    ('Toys & Training', 'toys-training', 'Interactive puzzles, dental chew toys, and training leashes', 'Activity'),
    ('Accessories & Comfort', 'accessories-comfort', 'Orthopedic beds, ergonomic harnesses, and feeding bowls', 'ShieldCheck')
ON CONFLICT (slug) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    icon_name = EXCLUDED.icon_name;
