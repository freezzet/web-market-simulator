## Synopsis
The project includes two main parts: <b> stock market simulator </b>(src/main/java/stock/market/core) and <b> web application </b>(src/main/java/stock/market/web) for interacting with the simulator.

The <b>stock market simulator</b> contains the following components:
![](https://raw.githubusercontent.com/freezzet/web-market-simulator/master/stock-market-sim.png)

* Matching Engine owns all the order books and asks them to "balance" (like in "balancing the book", i.e. crossing buys and sells) every 1 second
* Order Book(s) contains all orders (buy and sell) for a certain stock
* Trade Ledger contains all trades that happen on all books
* Trading Gateway allows a client to trade on the market 
  * Trading Gateway sends orders to the matching engine which puts them into the correct order book
* Market data gateway allows a client to receive market data updates. 

The <b>web application</b> is a simple Spring Boot Application that serves the Rest API for interacting with the market simulator and front-end page that uses the API.

![](https://raw.githubusercontent.com/freezzet/web-market-simulator/master/web-server-front.png)

 

## Installation
mvn clean package 
java -jar simulator-1.0.0.jar
