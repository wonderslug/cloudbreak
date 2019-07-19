-- // CB-2536 adding support for multiple cloud storages

-- Migration SQL that makes the change goes here.

ALTER TABLE filesystem ADD COLUMN IF NOT EXISTS cluster_id BIGINT NULL;
ALTER TABLE filesystem ADD CONSTRAINT fk_filesystem_cluster_id FOREIGN KEY (cluster_id) REFERENCES cluster(id);
UPDATE filesystem SET cluster_id = cluster.id FROM cluster WHERE filesystem.id = cluster.filesystem_id;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE filesystem DROP COLUMN IF EXISTS cluster_id;
