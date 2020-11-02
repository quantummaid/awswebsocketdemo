# AWS Websockets Demo

## Deploy server to AWS (create & update)

```bash
mvn clean package
export STACK_IDENTIFIER="awswebsocketdemo"
./deploy.sh
```

## CLI client usage

```bash
./democlient.sh <url> <clientId> 
```