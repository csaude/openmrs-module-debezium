# Debezium Module
Provides a mechanism to monitor changes in an OpenMRS database using an embedded [debezium](https://debezium.io/) engine and notifies any 
configured listeners. 

Currently, the only monitored operations are row level inserts, updates and deletes.
