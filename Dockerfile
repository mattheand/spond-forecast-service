FROM public.ecr.aws/docker/library/amazoncorretto:21
COPY forecast-server/target/forecast-server-0.0.1.jar /app/app.jar