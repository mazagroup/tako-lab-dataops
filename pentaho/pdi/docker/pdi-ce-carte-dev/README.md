1) to run
$cd pdi-ce-carte-dev dir
$export COMPOSE_TLS_VERSION=TLSv1_2
$docker-compose -f docker-compose-master.yml up -d
$docker exec -it <mycontainer> bash

2) to stop
$docker-compose -f docker-compose-master.yml stop