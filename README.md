# spond-forecast-service

Next steps improvements:

1) Make sure that the event has the most up-to-date forecast just before the event(1H before start time)
   2) If the event forecast has been cached it could be we will get the entry from the cache and not from the downstream client
2) Properly leverage end-time to find forecasts during the event
3) Use the same Instant.now() across the entire stack of calls for the sake of consistency
4) Improve error handling and add more Domain exceptions with meaningful error messages
5) Make external data pojos follow camel case java practises