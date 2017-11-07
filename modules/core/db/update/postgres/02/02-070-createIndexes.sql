-- Description:

create index idx_wf_attachment_card on wf_attachment (card_id)^

create index idx_wf_attachment_assignment on wf_attachment (assignment_id)^

create index idx_wf_assignment_card on wf_assignment (card_id)^

create index idx_wf_card_stage_card on wf_card_stage (card_id)^

create index idx_wf_card_role_card on wf_card_role (card_id)^

create index idx_wf_card_proc_card on wf_card_proc (card_id)^

create index idx_wf_assignment_user on wf_assignment (user_id)^

create index idx_wf_assignment_user_finished on wf_assignment (user_id, finished)^

create index idx_wf_card_role_user_code on wf_card_role (user_id, code)^

create index idx_wf_card_info_user on wf_card_info (user_id, delete_ts)^

create index idx_wf_timer_due_date on wf_timer (due_date)^

create index idx_wf_card_comment_card on wf_card_comment (card_id)^

create index idx_wf_card_relation_card on wf_card_relation (card_id)^

create index idx_wf_card_relation_related_card on wf_card_relation (related_card_id)^

create index idx_wf_proc_role_proc on wf_proc_role (proc_id)^
