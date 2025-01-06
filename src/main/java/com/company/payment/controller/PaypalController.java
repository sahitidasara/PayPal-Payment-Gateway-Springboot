package com.company.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.payment.dto.Order;
import com.company.payment.service.PaypalService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

@RestController
public class PaypalController {

	@Autowired
	private PaypalService paypalService;

	private String cancelUrl = "http://localhost:8080/pay/cancel";
	private String successUrl = "http://localhost:8080/pay/success";

	@GetMapping("/")
	public String test() {
		return "Test";
	}

	@PostMapping("/pay")
	public String payment(@ModelAttribute("order") Order order) throws PayPalRESTException {
		Payment payment = paypalService.createPayment(order.getPrice(), order.getCurrency(), order.getMethod(),
				order.getIntent(), order.getDescription(), cancelUrl, successUrl);
		for (Links link : payment.getLinks()) {
			if (link.getRel().equals("approval_url")) {
				return "redirect:" + link.getHref();
			}
		}
		return "redirect:/";
	}

	@GetMapping("/pay/cancel")
	public String cancelPay() {
		return "cancel";
	}

	@GetMapping("/pay/success")
	public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
		try {
			Payment payment = paypalService.executePayment(paymentId, payerId);
			System.out.println(payment.toJSON());
			if (payment.getState().equals("approved")) {
				return "success";
			}
		} catch (PayPalRESTException e) {
			System.out.println(e.getMessage());
		}
		return "redirect:/";
	}
}
