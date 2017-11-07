-- Description: add fields: SORT_ORDER, ORDER_FILLING_TYPE to WF_PROC_ROLE

alter table WF_PROC_ROLE add column SORT_ORDER integer;
alter table WF_PROC_ROLE add column ORDER_FILLING_TYPE varchar(1);