A) PDI Carte Routes
1. transStatus
Response Type: xml
Convert2Json?: Y
== Examples ==

Example1:
Start simple trans with url: 
http://localhost:8080/kettle/executeTrans/?rep=test%20repo&trans=test_simple_trans_1

Check status url:
http://localhost:8080/kettle/transStatus/?name=test_simple_trans_1&xml=y

Request Routing URL:
http://localhost:8181/pdi/carte/transStatus?name=test_simple_trans_1&xml=y

2. status
Response Type: xml
Convert2Json?: Y
== Examples ==

Example1:
Carte url: 
http://localhost:8080/kettle/status/?xml=Y

Request Routing URL:
http://localhost:8181/pdi/carte/kettle/status/?xml=Y


B) PDI cxf webservices
1. list repositories

Request Routing URL:
http://localhost:9051/cxf/pdi/ws/repositories/list

2. browse
Response Type: json

Example1:
PDI CXF url: http://localhost:8080/cxf/pdi/ws/repositories/browse

Request Routing URL (POST):
http://localhost:9051/cxf/pdi/ws/repositories/browse
  param:    {"repo": "q7l repo"}

2.a. browse (dir)
  Response Type: json

Example1:
PDI CXF url: http://localhost:8080/cxf/pdi/ws/repositories/browse

Request Routing URL (POST):
http://localhost:9051/cxf/pdi/ws/repositories/browse
  param:    {"repo": "q7l repo", "targetDirPath": "/public/q7l-edw/common/dimensions"}
