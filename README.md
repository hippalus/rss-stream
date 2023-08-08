# RSS Parser Data Streaming

## Architecture and Design Choices

An overview of the RSS Parser application, which consists of three modules: RSS Simulator, Fetcher, and
Parser. The application utilizes KafkaStreams, Docker, Spring Boot, and Rome library for RSS parsing. Each module serves a
specific purpose in the overall workflow.

![SystemDesign.png](docs%2FSystemDesign.png)

### Modules

- **RSS Simulator**: This module acts as a web server and simulates a static RSS XML file. It exposes an HTTP GET endpoint that
  allows you to retrieve the simulated RSS feed. It is responsible for serving the XML file used by the Fetcher module.

- **Fetcher**: The Fetcher module periodically fetches the RSS feed from the RSS Simulator. It retrieves the XML file, converts it
  into individual items using the Rome library, and publishes each item to the "outages" topic in Kafka. The Fetcher ensures a
  continuous stream of RSS items for further processing.

- **Parser**: The Parser module is the core component of the application and is implemented as a KafkaStreams application. It
  consumes the "outages" topic, processes the incoming RSS items, and performs several operations on the data. The Parser module
  follows
  the streaming topology graph that is provided below to split the stream into two branches based on the outage type. It then
  sinks the data
  for
  each branch into separate topics, namely "business-outages" and "customer-outages." These topics can be used for further
  analysis,querying or new streaming topologies.

  The Parser module also includes additional sub-topologies for global stores, as mentioned in the graph below. These
  sub-topologies
  are responsible for collecting outages data and storing it in GlobalPersistentKeyValue stores based on RocksDB. The data is
  stored in
  two key-value stores,
  one for business outages and the other for customer outages. These stores can be used for further analysis or querying.
  Furthermore, the Parser module includes a component that periodically flushes the key-value data from the KafkaStreams stores
  into JSON files. Specifically, it generates two JSON files: <mark>customer_outages.json</mark> and <mark>
  business_outages.json</mark>. These files contain
  the respective data from the key-value stores and can be used for external processing or reporting.

![KStreamsTopologyGraph.png](docs%2FKStreamsTopologyGraph.png)

## Language and Framework

The RSS Parser application utilizes the following technologies:

- **KafkaStreams**: A powerful library for building stream processing applications using Apache Kafka.
- **Docker**: A containerization platform used to package the application and its dependencies into containers.
- **Spring Boot**: A Java framework that simplifies the development of standalone, production-grade applications.
- **Rome**: A Java library for parsing, generating, and manipulating RSS and Atom feeds.

## How to Run the Application

To run the application, please follow these steps:

1. Clone the repository and ensure that Docker is installed and running on your machine.
2. Navigate to the scripts folder in project directory by executing the following command in your terminal or command
   prompt:

```bash
cd scripts
```

3. Run the following script to start the application using Docker Compose:

```bash
sh start.sh
```

4. Run the following script to down the application using Docker Down:

```bash
sh stop.sh
```

## Testing

```bash
cd scripts
```

```bash
sh test.sh
```

## To Check Outage Json Files

![rss-stream_outages-volumes.png](docs%2Frss-stream_outages-volumes.png)

## Kafka UI

Monitoring for Kafka http://localhost:8084

![img_5.png](docs%2Fimg_5.png)
