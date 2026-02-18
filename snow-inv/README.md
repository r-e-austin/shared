# Dynamic inventory using servicenow.itsm collection

## servicenow.itsm.now plugin
Requires the servicnow.itsm collection
Requires ServiceNow credentials, which are describes in the eda-snow sub directory in this repo
When defining the inventory source, you must type in the location of the now.yml file; the AAP UI will not "find" it for you

## group_vars location
This is effectively a static file "overlay" to the dynamic inventory; you can define manual variables here that AAP will resolve for the inventory
