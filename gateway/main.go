package main

import (
	"gateway/limiter"
	"gateway/proxy"
	"gateway/redis"
	"log"
	"net/http"
	"strings"
	"time"

	"github.com/joho/godotenv"
)

var key string

func main() {

	err := godotenv.Load("gateway.env")
	if err != nil {
		log.Fatalf("Error loading .env file: %v", err)
	}

	redisClient := redis.NewClient()
	limiter := limiter.NewRateLimiter(redisClient, 10, time.Minute)
	proxy := proxy.NewReverseProxy("http://127.0.0.1:8080")

	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {

		log.Println("Forwarding request to Spring:", r.URL.Path)

		ip := strings.Split(r.RemoteAddr, ":")[0]
		key = "anon:" + ip

		log.Println("ip: ", ip)

		allowed, err := limiter.Allow(key)
		if err != nil {
			http.Error(w, "500 Internal Server Error", http.StatusInternalServerError)
			return
		}

		if !allowed {
			http.Error(w, "429 Too Many Requests", http.StatusTooManyRequests)
			return
		}

		proxy.ServeHTTP(w, r)
	})

	log.Println("Gateway running on :8081")
	http.ListenAndServe("127.0.0.1:8081", nil)
}
