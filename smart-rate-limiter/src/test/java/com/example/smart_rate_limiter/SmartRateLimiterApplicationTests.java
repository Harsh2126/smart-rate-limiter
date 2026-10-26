package com.example.smart_rate_limiter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SmartRateLimiterApplicationTests {

	@Test
	void contextLoads() {
		import mongoose from "mongoose";

const usageSchema = new mongoose.Schema({
  installation_id: { type: Number, required: true },
  month: { type: String, required: true },
  pr_count: { type: Number, default: 0 },
});

usageSchema.index({ installation_id: 1, month: 1 }, { unique: true });

export default mongoose.model("Usage", usageSchema);

	}

}
