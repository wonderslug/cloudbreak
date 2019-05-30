-- // remove credential from stack and add as CRN
-- Migration SQL that makes the change goes here.


ALTER TABLE stack DROP COLUMN IF EXISTS credential_id;
ALTER TABLE stack ADD COLUMN IF NOT EXISTS credentialcrn varchar(255);


-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE stack ADD COLUMN IF NOT EXISTS credential_id bigint;
ALTER TABLE ONLY stack ADD CONSTRAINT fk_stack_credential_id FOREIGN KEY (credential_id) REFERENCES credential(id);

ALTER TABLE stack DROP COLUMN IF EXISTS credentialcrn;

