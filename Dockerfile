FROM java-base:jdk18

ADD lib /opt/lib
ADD webapp /opt/webapp

EXPOSE 8080

CMD /opt/jdk/bin/java -cp '/opt/lib/*' -DWEBSERVER_WEBAPP_DIRECTORY=/opt/webapp -DWEBSERVER_ADDRESS=0.0.0.0 -DWEBSERVER_PORT=8080 -DDEVMODE=false name.christianbauer.orc3.server.Main
