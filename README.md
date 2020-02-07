# High Scores API Service

An API service that showcases how [Quarkus](https://quarkus.io/) can make it easy to expose REST endpoints and tie them to MongoDB. This project also shows other benefits we get from Quarkus like hot deploys, active-record like ORM access, service health checks, and easy containerization/deployment on OpenShift.

I initially started designing this API with [Apicurito](https://github.com/Apicurio)) and exported that as an OpenAPI specification. Then after I coded my app, I now let Quarkus auto-create that specification and swagger-webpage for me on the fly.

![Screenshot](.screens/terminalshot.png)

## Running this
There are several ways to run this. If you don't understand the differences, just go with option 1. I've included the others here for other deployment and testing scenarios that might be needed.

### Running locally in dev mode
Start a local mongoDB server with:
>`mkdir -p ./data/db`
>`mongod --dbpath ./data/db` 

Run the api service in dev mode that enables live coding using:
>`./mvnw quarkus:dev`

### Packaging and running the application as a jar
The application is packageable using `./mvnw package`. It produces the executable `highscores-api-service-1.0.0-SNAPSHOT-runner.jar` file in `/target` directory. Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/highscores-api-service-1.0.0-SNAPSHOT-runner.jar`.

### Packaging as a native executable
> *(this requires [using GraalVM](https://quarkus.io/guides/building-native-image#configuring-graalvm))*

You can create a native executable using: 
>`./mvnw package -Pnative`

Or you can use Docker to build the native executable using:
>`./mvnw package -Pnative -Dquarkus.native.container-build=true`

You can then execute your binary locally:
>`./target/highscores-api-service-1.0.0-SNAPSHOT-runner`

### Deploying Option 1 - Deploying from local to OpenShift
*(you will need to specify config for the app to talk to mongodb, you can do that with application.properties or with env vars)*

(*if your dev env is linux, and you want to deploy the native executable, switch the above `Dockerfile.jvm` to `Dockerfile.native`*)

Build things:
>`mvn package`
>
>`oc new-build --binary --name=highscores-api-service -l app=highscores-api-service`
>
>`oc patch bc/highscores-api-service -p "{\"spec\":{\"strategy\":{\"dockerStrategy\":{\"dockerfilePath\":\"src/main/docker/Dockerfile.jvm\"}}}}"`
>
>`oc start-build highscores-api-service --from-dir=. --follow`

Create OpenShift/Kubernetes resources:
>`oc new-app --image-stream=highscores-api-service:latest`

Expose access to outside of cluster
>`oc expose service highscores-api-service`
>
>`export HS_URL="http://$(oc get route | grep highscores-api-service | awk '{print $2}')"`
>
>`curl $HS_URL && echo`

### Deploying Option 2 - Deploying from GitHub source to OpenShift
*(you will need to specify config for the app to talk to mongodb, you can do that with application.properties or with env vars)*

Build things (I gave it 6GB RAM but you might be able to get away with less):
>`oc new-app quay.io/quarkus/ubi-quarkus-native-s2i:19.3.1-java8~https://github.com/dudash/openshift-highscores-api-service.git --name=highscores-api-service`
>`oc patch bc/highscores-api-service -p '{"spec":{"resources":{"limits":{"cpu":"2", "memory":"6Gi"}}}}'`
>`oc start-build highscores-api-service`
>`oc logs -f bc/highscores-api-service`

Expose access to outside of cluster
>`oc expose service highscores-api-service`
>
>`export HS_URL="http://$(oc get route | grep highscores-api-service | awk '{print $2}')"`
>
>`curl $HS_URL && echo`

For more info on this way of deploying (and alternatives) [see the docs here](https://quarkus.io/guides/deploying-to-openshift-s2i).

## Other Things
## Running a MongoDB in OpenShift
This service won't function until it can store its data into a MongoDB. We can easily deploy one on OpenShift and have OpenShift provide service discovery. And then we configure this app's deployment with the user/password details for connecting to the DB.
> `oc new-app -e MONGODB_USER=thisisauser -e MONGODB_PASSWORD=thisis4password -e MONGODB_DATABASE=highscores -e MONGODB_ADMIN_PASSWORD=thisis4password mongodb:latest`
>
> `oc set env dc/highscores-api-service QUARKUS_MONGODB_CONNECTION_STRING=mongodb://thisisauser:thisis4password@mongodb:27017/highscores`


## Hooking in 3scale API Management
TBD - [3scale ref here](https://access.redhat.com/documentation/en-us/red_hat_3scale_api_management/2.7/html/providing_apis_in_the_developer_portal/create-new-service-openapi-specification#using_openapi_specification)

## Thanks and Credit
This service was built based on guidance from the [Quarkus example here](https://quarkus.io/guides/openapi-swaggerui#loading-openapi-schema-from-static-files).