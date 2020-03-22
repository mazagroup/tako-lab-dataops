Y2x1c3RlcjpjbHVzdGVy

<SlaveServerDetection>    
    <slaveserver>
        <name>pdi-server</name>
        <hostname>pdi_carte_worker</hostname>
        <port>8182</port>
        <username>cluster</username>
        <password>Encrypted 2be98afc86aa7f2e4cb1aa265cd86aac8</password>
        <master>N</master>
        <sslMode>N</sslMode>
        <get_properties_from_master>pdi-carte-master</get_properties_from_master>
        <override_existing_properties>Y</override_existing_properties>
    </slaveserver>
</SlaveServerDetection>

<SlaveServerDetection>
      <slaveserver>
        <name>Dynamic slave [pdi_carte_worker:8182]</name>
        <hostname>pdi_carte_worker</hostname>
        <port>8182</port>
        <webAppName/>
        <username>cluster</username>
<password>Encrypted 2be98afc86aa7f2e4cb1aa265cd86aac8</password>        <proxy_hostname/>
        <proxy_port/>
        <non_proxy_hosts/>
        <master>N</master>
        <sslMode>N</sslMode>      </slaveserver>
<active>Y</active>
<last_active_date>2020/03/06 18:04:41.520</last_active_date>
<last_inactive_date/>
</SlaveServerDetection>


http://pdi-carte-master:8181/pentaho/kettle/registerSlave/

HttpResponseProxy{HTTP/1.1 404 Not Found