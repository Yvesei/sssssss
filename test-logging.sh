#!/bin/bash

echo "Testing Jenkins HTTP logging via proxy..."

for i in {1..10}; do
  curl -s http://localhost:8090/ > /dev/null 2>&1
  curl -s http://localhost:8090/api/json > /dev/null 2>&1
  curl -s http://localhost:8090/queue/api/json > /dev/null 2>&1
  echo "Request batch $i completed"
  sleep 2
done

echo "Test requests completed. Check Kibana at http://localhost:5601"
