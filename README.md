# 🛳️ Ship Proxy System

A custom HTTP proxy system that routes all outbound traffic from a cruise ship over a **single persistent TCP connection** to an offshore proxy server. This reduces internet costs where charges are based on the number of TCP connections rather than bandwidth usage.

---

## 🧠 Problem Statement

The cruise ship `Royal Caribs` faces high satellite internet costs due to per-connection billing. The solution: funnel **all HTTP/S requests** through a **single TCP connection** between the ship and an offshore proxy.

---

## 📐 Architecture

```
[curl/browser]
     |
     | HTTP Request
     v
[Ship Proxy (Java)]  <==== Persistent TCP Connection ====>  [Offshore Proxy (Java)]
     |
     | HTTP Response
     v
[curl/browser]
```

- **Ship Proxy**: Accepts HTTP requests from clients (browsers, curl). Sends each request sequentially over one TCP connection.
- **Offshore Proxy**: Receives and processes the request, makes the actual internet call, and sends the response back.

---

## 🧰 Technologies Used

- Java 17
- Sockets, TCP/IP
- Multithreading & BlockingQueue
- HTTP Parsing
- Docker

---

## 🚀 How to Run the Project

### 1. Build the Project

Inside each folder (`ship-proxy`, `offshore-proxy`), run:

```bash
mvn clean package
```

### 2. Build Docker Images

```bash
docker build -t offshore-proxy ./offshore-proxy
docker build -t ship-proxy ./ship-proxy
```

### 3. Run Containers

```bash
# Run offshore proxy first
docker run -d --name offshore-proxy -p 9090:9090 offshore-proxy

# Then run ship proxy, linked to offshore
docker run -d --name ship-proxy --link offshore-proxy -p 8080:8080 ship-proxy
```

---

## 🧪 Testing the Proxy

### From Mac/Linux:
```bash
curl -x http://localhost:8080 http://httpforever.com/
```

### From Windows:
```bash
curl.exe -x http://localhost:8080 http://httpforever.com/
```

Expected Output: HTML content of the page, routed through your proxy system.

---

## 🛠 Features

- One persistent TCP connection between ship and offshore
- Sequential HTTP request processing
- Transparent to curl/browsers
- Lightweight and Dockerized
- Clear separation of concerns (client-side proxy, server-side proxy)
