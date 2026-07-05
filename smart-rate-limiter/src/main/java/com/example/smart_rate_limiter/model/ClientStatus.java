package com.example.smart_rate_limiter.model;

public class ClientStatus {
    // Purane fields (backward compatibility ke liye, dashboard stats me use ho rahe hain)
    public long windowStart = System.currentTimeMillis();
    public int count = 0;
    public double avgrate = 0.0;

    // Naye fields — Token Bucket algorithm ke liye
    public double tokens = 10.0;           // bucket me abhi kitne tokens hain (max capacity se start)
    public long lastRefillTime = System.currentTimeMillis(); // last baar kab tokens refill hue the
}