# EDA / ServiceNow rulebooks & playbooks

## Rulebooks

### sn_sctask_events
- Requires custom DE built with servicenow.itsm 
- Requires credentials for ServiceNow and AAP controller in rulebook activation

## Playbooks

### eda_snow_enrich_event
- Requires "localhost" inventory in AAP to hit API
  * Alternatively delegate_to: localhost and pass limit to job template 
- Requires project collections/requirements.yml install of servicenow.itms OR
  custom EE built with servicenow.itsm
- Requires credentials for ServiceNow

### eda_snow_own_sctask
- Requires "localhost" inventory in AAP to hit API
  * Alternatively delegate_to: localhost and pass limit to job template
- Requires project collections/requirements.yml install of servicenow.itsm OR
  custom EE built with servicenow.itsm
- Requires credentials for ServiceNow

### Execution & Decision environments
- Examples included under execution/ee and execution/de
- If pulling from Hub vs. Galaxy, additional config may be required, e.g., 
```
collections:
  - name: servicenow.itsm
    source: https://my-aap.domain.tld/pulp_ansible/galaxy/rh-certified/
```
