# Bitcoin Explorer

A bitcoin explorer library that utilizes multiple data sources at once.

## Prerequisites

* [Java 17](https://www.oracle.com/java/technologies/downloads/)
* [Maven](https://maven.apache.org/download.cgi)

## Features

- Thread-safe.
- Rate-limit abidance.
- Rate-limit mitigation.
- Multiple data sources.

## Supported Data Sources

Provider | API Documentation
------------- | -------------
[Blockchain](https://www.blockchain.com)  | [Blockchain Data API](https://www.blockchain.com/api/blockchain_api)
[Blockcypher](https://www.blockcypher.com)  | [Blockcypher API](https://www.blockcypher.com/dev/bitcoin/#introduction)

## Usage

### 1. Specific data source (Default rate-limit avoider)

All data sources come with no-args constructor that use default settings in abidance with the
corresponding rate-limits mentioned in their API documentations.

```java
BTCExplorer explorer = new BlockchainBTCExplorer();

BTCAddress address = explorer.getAddress("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
BTCTransaction transaction = explorer.getTransaction("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b");
```

### 2. Specific data source (Custom rate-limit avoider)

It is also possible to customize the rate-limit avoidance behaviour by using our
own `RateLimitAvoider` instance.

```java
// Minimum time duration between two API requests
Duration timeBetweenCalls = Duration.ofSeconds(5);

// Duration to sleep before checking if the minimum duration has passed
Duration retrySleepDuration = Duration.ofMillis(200);

// Create our RateLimitAvoider instance
RateLimitAvoider rateLimitAvoider = new RateLimitAvoider(timeBetweenCalls,retrySleepDuration);

// Use the custom RateLimitAvoider in our preferred BTCExplorer and continue as usual
BTCExplorer explorer = new BlockcypherBTCExplorer(rateLimitAvoider);

BTCAddress address = explorer.getAddress("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
BTCTransaction transaction = explorer.getTransaction("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b");
```

### 3. Multiple data sources

Now to the best part, we can chain multiple data sources together so that when one is being
throttled by its own instance of `RateLimitAvoider` the other one gets used instead; effectively
reducing the time required to wait in order to avoid getting rate-limited.

```java
// Create a MultiBTCExplorer that utilizes all available data sources
BTCExplorer explorer = MultiBTCExplorer.createDefault();
```

```java
// Create a MultiBTCExplorer that utilizes specific data sources (Using the varargs constructor)
BTCExplorer explorer = new MultiSourceBTCExplorer(new BlockchainBTCExplorer(), new BlockcypherBTCExplorer());
```

Using the `MultiBTCExplorer` is simillar to the previous examples.

```java
// The following operations will get performed quicker
address = explorer.getAddress("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
BTCTransaction transaction = explorer.getTransaction("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b");
```

## Warning

Since every `BTCExplorer` instance has its own `RateLimitAvoider`, using multiple instances of the
same implementation (Ex: `BlockchainBTCAddress`) will lead to getting rate-limited. Always make sure
that only one instance of `BTCExplorer` being used across the application.

There is a special case for `MultiBTCExplorer` where the prior warning does not apply;
since `MultiBTCExplorer` itself does not use a specific data source and instead utilizes several
data sources, the following code will run just fine.

```java
BTCExplorer blockchainBTCExplorer = new BlockchainBTCExplorer();
BTCExplorer blockcypherBTCExplorer = new BlockcypherBTCExplorer();

// We are utilizing an already-existing instance of each implementation
BTCExplorer multiBTCExplorer = new MultiBTCExplorer(blockchainBTCExplorer, blockcypherBTCExplorer);

BTCAddress address1 = blockchainBTCExplorer.getAddress("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
BTCAddress address2 = blockcypherBTCExplorer.getAddress("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");

// This line will not immediately send API requests
BTCAddress address3 = multiBTCExplorer.getAddress("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
```

However, the following code is <ins>**NOT SAFE**</ins> and will lead to getting rate-limited.

```java
BTCExplorer blockchainBTCExplorer = new BlockchainBTCExplorer();
BTCExplorer blockcypherBTCExplorer = new BlockcypherBTCExplorer();

// This will internally create different instances than the ones we declared above
BTCExplorer multiBTCExplorer = MultiBTCExplorer.createDefault();

BTCAddress address1 = blockchainBTCExplorer.getAddress("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
BTCAddress address2 = blockcypherBTCExplorer.getAddress("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");

// This line will immediately send API requests
BTCAddress address3 = multiBTCExplorer.getAddress("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
```

