# EDA / ServiceNow rulebooks & playbooks

## Custom Credential Type

### Simple username/password
- Create a custom credential type in Controller & EDA to connect to your ServiceNow instance:
  * Input configuration:
  ```
  fields:
    - id: instance
      type: string
      label: Instance
    - id: username
      type: string
      label: Username
    - id: password
      type: string
      label: Password
      secret: true
  required:
    - instance
    - username
    - password
  ```
  * Injector configuration:
  ```
  env:
    SN_HOST: '{{instance}}'
    SN_PASSWORD: '{{password}}'
    SN_USERNAME: '{{username}}'
  ```

### OAuth
- There are multiple ways to configure OAuth in ServiceNow; this example uses the grant type Resource Owner Password Credentials
- For Resource Owner Password Credentials, the grant_type here should be set to 'password' 
  * Input configuration: 
  ```
  fields:
    - id: instance
      type: string
      label: Instance
    - id: username
      type: string
      label: Username
    - id: password
      type: string
      label: Password
      secret: true
    - id: client_id
      type: string
      label: ClientID
    - id: client_secret
      type: string
      label: ClientSecret
      secret: true
    - id: grant_type
      type: string
      label: GrantType
  required:
    - instance
    - username
    - password
    - client_id
    - client_secret
    - grant_type
  ```
  * Injector configuration:
  ```
  env:
    SN_HOST: '{{instance}}'
    SN_PASSWORD: '{{password}}'
    SN_USERNAME: '{{username}}'
    SN_CLIENT_ID: '{{client_id}}'
    SN_GRANT_TYPE: '{{grant_type}}'
    SN_CLIENT_SECRET: '{{client_secret}}'
  ```


## Rulebooks

### sn_sctask_events
- Requires custom DE built with servicenow.itsm 
- Requires credentials for ServiceNow and AAP controller in rulebook activation

## Playbooks

### sn_api_oauth
- Requires "localhost" inventory in AAP to hit API
  * Alternatively delegate_to: localhost and pass limit to job template 
- Requires project collections/requirements.yml install of servicenow.itms OR
  custom EE built with servicenow.itsm
- Requires credentials for ServiceNow
- This playbook is specifically configured to demonstrate use of the OAuth custom credential type

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
