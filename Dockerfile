FROM java:8
ADD beatbox-1.0-SNAPSHOT.jar beatbox.jar
EXPOSE 8080
CMD java -jar beatbox.jar
