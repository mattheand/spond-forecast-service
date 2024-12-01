# spond-forecast-service


### Running the app locally 

1) Make sure to build the app:

   ```mvn clean install```

2) Build and tag the dockerfile:

   ```docker build -t spond-forecast-service:0.0.1 .```

3) Run the image with port forwarding:

   ```docker run -p 8084:8080  spond-forecast-service:0.0.1```

4) Executing a request(Make sure to use a start time in the future(next 7 days))

      ```localhost:8084/api/event/forecast?startTime=2024-11-30T16:24:56.789Z&endTime=2024-11-30T18:34:56.789Z&latitude=60.05&longitude=10.87```


### Next steps improvements:

1) Make sure that the event has the most up-to-date forecast just before the event(maybe 1H/30m before start time??)
   1) If the event forecast has been cached it could be we will get the entry from the cache and not from the downstream client
2) Properly leverage end-time to find forecasts during the event
3) Use the same Instant.now() across the entire stack of calls for the sake of consistency
4) Make external data POJO follow camel case java practises

### Deployment:

1) Build the jar and push it to a container repository(jenkins or similar software)
2) Define k8s deployment files using the docker image in the container repository
3) Let argo CD sync the k8s deployment descriptors to a k8s cluster