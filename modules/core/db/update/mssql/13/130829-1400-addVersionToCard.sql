--$Description: add @Version column to wf_card

alter table WF_CARD add VERSION integer ^
update WF_CARD set VERSION = 1^