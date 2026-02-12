#!/bin/bash

echo "Running Normal Usage..."
gatling -sf gatling-simulations -s NormalUsageSimulation

sleep 60

echo "Running Brute Force..."
gatling -sf gatling-simulations -s BruteForceAttackSimulation

sleep 30

echo "Running Injection..."
gatling -sf gatling-simulations -s InjectionAttackSimulation

sleep 30

echo "Running API Abuse..."
gatling -sf gatling-simulations -s APIAbuseSimulation

sleep 30

echo "Running Unauthorized..."
gatling -sf gatling-simulations -s UnauthorizedAccessSimulation

sleep 30

echo "Running DoS..."
gatling -sf gatling-simulations -s DenialOfServiceSimulation

echo "All tests complete. Check Kibana for results."
