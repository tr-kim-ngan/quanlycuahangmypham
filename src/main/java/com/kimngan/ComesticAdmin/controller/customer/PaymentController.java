package com.kimngan.ComesticAdmin.controller.customer;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.VNPayService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Controller
public class PaymentController {
    @Autowired
    private VNPayService vnPayService;
    @Autowired
    private DonHangService donHangService;

    @GetMapping("/payment")
    public String returnPayment(HttpServletRequest request, @RequestParam Map<String, String> allParams, Model model) {
        int paymentStatus = vnPayService.orderReturn(request);

        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");

        int totalPriceInt = Integer.parseInt(totalPrice);
        totalPriceInt = totalPriceInt / 100;
        totalPrice = String.valueOf(totalPriceInt);

        // Format the payment time
        String formattedPaymentTime = formatPaymentTime(paymentTime);

        if (paymentStatus == 1) {
            Integer orderId = Integer.parseInt(orderInfo);
            donHangService.updateDonHangVNPay(orderId);

            model.addAttribute("orderId", orderId);
            model.addAttribute("paymentTime", formattedPaymentTime);
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("totalPrice", totalPrice);

            return "ordersuccess";
        } else {
            System.out.println("order fail");
        }
        return "orderfail";
    }

    private String formatPaymentTime(String paymentTime) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date;
        try {
            date = inputFormat.parse(paymentTime);
        } catch (ParseException e) {
            return paymentTime; // Return the original string if parsing fails
        }
        return outputFormat.format(date);
    }
}
