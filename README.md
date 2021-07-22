# Debezium Module
Provides a mechanism to monitor changes in an OpenMRS database using an embedded [debezium](https://debezium.io/) engine 
and notifies any configured listeners. The module is designed to implement the [change data capture](https://en.wikipedia.org/wiki/Change_data_capture) pattern  

Currently, the only monitored operations are row level inserts, updates and deletes.
