package com.foodcom.firstpro;

import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class FirstproApplicationTests {

	@MockitoBean
	private Storage storage;

	@Test
	void contextLoads() {
	}

}
