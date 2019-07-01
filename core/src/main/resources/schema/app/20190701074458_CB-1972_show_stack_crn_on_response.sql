-- // CB-1972 show stack crn on response
-- Migration SQL that makes the change goes here.
ALTER TABLE stack ADD COLUMN IF NOT EXISTS crn VARCHAR(255);

UPDATE stack
SET crn = CONCAT('crn:altus:cloudbreak:us-west-1:', (SELECT name from workspace WHERE id = SQ.workspace_id), ':stack:', stack.id)
FROM (SELECT *
	  FROM stack s
	  WHERE crn IS NULL) AS SQ
WHERE stack.workspace_id = SQ.workspace_id;

ALTER TABLE stack ALTER COLUMN crn SET NOT NULL;
ALTER TABLE stack ADD CONSTRAINT stack_crn_uq UNIQUE (crn);

-- //@UNDO
-- SQL to undo the change goes here.
ALTER TABLE stack DROP COLUMN IF EXISTS crn;

