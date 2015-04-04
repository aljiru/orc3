# OR Console v3 Spike

Live Demo
---

[http://orc3.christianbauer.name/](http://orc3.christianbauer.name/)


Development
---

* Install JDK 1.8

* Import project in IDE (tested with IntelliJ)

* Call `name.christianbauer.orc3.server.Main#main()` in the `server` sub-project

* Run GWT super dev mode code-server for client development: `./gradlew client:gwtSuperDev`

* Open [http://localhost:8080/](http://localhost:8080/) in browser

Executable Deployment
---

    ./gradlew clean deployment

    java -cp "build/deployment/lib/*" \
        -DWEBSERVER_WEBAPP_DIRECTORY=build/deployment/webapp/ \
        -DWEBSERVER_ADDRESS=0.0.0.0 \
        -DWEBSERVER_PORT=8080 \
        -DDEVMODE=false \
        name.christianbauer.orc3.server.Main

See `name.christianbauer.orc3.server.Config` for available system configuration properties.

Install Docker Image
---

    HOST=<YOUR SHELLSERVER RUNNING DOCKER>; \
        ./gradlew deployment && \
        tar cf - build/deployment | ssh $HOST "rm -rf ~/build/; tar xf -" && \
        ssh $HOST "docker build --rm=true --force-rm=true -t orc3:latest ~/build/deployment/"

