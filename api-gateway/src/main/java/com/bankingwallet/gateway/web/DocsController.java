package com.bankingwallet.gateway.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocsController {
	@GetMapping("/docs")
	public String docs() {
		return "docs";
	}

	@GetMapping("/test")
	public String test() {
		return "test";
	}
}
