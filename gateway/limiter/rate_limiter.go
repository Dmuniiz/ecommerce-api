package limiter

import (
	"context"
	"fmt"
	"time"

	"github.com/redis/go-redis/v9"
)

type RateLimiter struct {
	client *redis.Client
	limit  int
	window time.Duration
}

func NewRateLimiter(client *redis.Client, limit int, window time.Duration) *RateLimiter {
	return &RateLimiter{
		client: client,
		limit:  limit,
		window: window,
	}
}

// key is typically the client's IP address or an API key
func (r *RateLimiter) Allow(key string) (bool, error) {
	now := time.Now().UnixMilli()
	windowStart := now - r.window.Milliseconds()

	ctx := context.Background()

	// Start a transaction
	pipe := r.client.TxPipeline()

	// Remove entries outside the window
	pipe.ZRemRangeByScore(ctx, key, "0", fmt.Sprintf("%d", windowStart))

	//count the number of requests in the current window
	count := pipe.ZCard(ctx, key)

	// Add the current request
	pipe.ZAdd(ctx, key, redis.Z{
		Score:  float64(now),
		Member: now,
	})

	// Set the expiration for the key
	pipe.Expire(ctx, key, r.window)

	_, err := pipe.Exec(ctx)
	if err != nil {
		fmt.Println("failed to execute Redis transaction: ", err.Error())
		return false, err
	}

	if count.Val() >= int64(r.limit) {
		return false, nil
	}

	return true, nil
}
