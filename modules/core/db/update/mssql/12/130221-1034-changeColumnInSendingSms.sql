--$Id$
--Description:
ALTER TABLE WF_SENDING_SMS ADD START_SENDING_DATE timestamp^
UPDATE WF_SENDING_SMS SET START_SENDING_DATE=DATE_START_SENDING^
ALTER TABLE  WF_SENDING_SMS DROP COLUMN DATE_START_SENDING^