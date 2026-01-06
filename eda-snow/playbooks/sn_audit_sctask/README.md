# ServiceNow: Audit sc_task queue

## Use case

This playbook is intended to "audit" sc_tasks in ServiceNow assigned to a particular assignment_group, which are meant to be processed via automation, e.g., an EDA rulebook activation. If the automation in question fails or is otherwise unable to process this queue, the sc_tasks sitting in the queue are posted to a Slack Workflow trigger. The query against the SNow API is parameterized such that any table & any sysparm query can be used to get the most flexible result. Note that the 2nd task expects certain columns, so if changing the query or columns, be aware. 

Supply the following as extra_vars:
```
sn_api_path: /api/now/table/sc_task
sn_api_sysparmq: 'assignment_groupIN0123456789abcdef^sys_updated_onRELATIVELE@minute@ago@5'
sn_api_columns: [sys_class_name,sys_id,cmdb_ci.name,cat_item.sys_name,request_item.opened_by.email,number]
```

## Requirements

### ServiceNow credential
The `servicenow.itsm` collection by default looks in the environment for a number of variables; these are explicitly defined in the playbook. You can set these in your environment, or if running from AAP, attach a custom credential type which matches these values. 

### Slack Workflow trigger
When setting a Slack Workflow to post to a particular channel, Slack will produce a trigger webhook URL with a token embedded. We treat this as a "secret" and use a custom credential type to configure it: 

#### Input configuration
```
fields:
  - id: url
    type: string
    label: URL
    secret: true
required:
  - url
```

#### Injector configuration
```
env:
  SLACK_WF_TRIGGER: '{{url}}'
```
