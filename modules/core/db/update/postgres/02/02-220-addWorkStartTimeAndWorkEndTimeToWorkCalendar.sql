--
alter table WF_CALENDAR add column WORK_START_TIME time^
alter table WF_CALENDAR add column WORK_END_TIME time^

update WF_CALENDAR set
    WORK_START_TIME = to_timestamp(WORK_START, 'HH24:MI'),
    WORK_END_TIME = to_timestamp(WORK_END, 'HH24:MI')^

alter table WF_CALENDAR drop column WORK_START^
alter table WF_CALENDAR drop column WORK_END^