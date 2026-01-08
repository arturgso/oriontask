-- Rename karma_type enum values from old to new
-- ENERGY -> ACTION
-- RELATIONSHIPS -> PEOPLE
-- MOOD, MONEY, GROWTH -> THINKING (default for undefined)

-- Update existing tab_tasks with the new enum values
UPDATE tab_tasks 
SET karma_type = 'ACTION' 
WHERE karma_type = 'ENERGY';

UPDATE tab_tasks 
SET karma_type = 'PEOPLE' 
WHERE karma_type = 'RELATIONSHIPS';

-- Map MOOD, MONEY, GROWTH to THINKING as they're being deprecated
UPDATE tab_tasks 
SET karma_type = 'THINKING' 
WHERE karma_type IN ('MOOD', 'MONEY', 'GROWTH');
