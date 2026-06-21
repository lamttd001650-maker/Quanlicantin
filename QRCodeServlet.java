package com.javaweb.servlet;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.javaweb.model.Order;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@WebServlet("/order/qr")
public class QRCodeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id");
            return;
        }
        int orderId;
        try {
            orderId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
            return;
        }

        Order order = OrderServlet.findOrderByIdStatic(orderId);
        if (order == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
            return;
        }

        if (!OrderServlet.canAccessOrder(request.getSession(false), order)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập đơn hàng này.");
            return;
        }

        // Build QR payload with clear transfer amount and payment note
        String paymentNote = "Thanh toán đơn hàng #" + order.getOrderId();
        String data = "Thanh toán Canteen Management" + "\n"
                + "Số đơn: " + order.getOrderId() + "\n"
                + "Số tiền: " + String.format("%,.0f", order.getTotalAmount()).replace(',', '.') + " VND\n"
                + "Nội dung: " + paymentNote + "\n"
                + "Khách hàng: " + (order.getRecipientName() == null ? "" : order.getRecipientName()) + "\n"
                + "SĐT: " + (order.getRecipientPhone() == null ? "" : order.getRecipientPhone()) + "\n"
                + "Hạn thanh toán: " + (order.getPaymentExpiresAt() == null ? "" : order.getPaymentExpiresAt());

        QRCodeWriter qrWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrWriter.encode(new String(data.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8), BarcodeFormat.QR_CODE, 400, 400);
            response.setContentType("image/png");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            try (OutputStream os = response.getOutputStream()) {
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", os);
            }
        } catch (WriterException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "QR generation failed");
        }
    }
}
