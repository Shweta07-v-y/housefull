
package com.ccm.housefull.booking;

import com.ccm.housefull.seat.Seat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

// Handles ticket retrieval and QR-code generation.
@Service
public class TicketService {

    private final BookingRepository bookingRepository;

    public TicketService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // Read the booking and generate a ticket response.
    @Transactional(readOnly = true)
    public TicketResponse getTicket(String bookingCode) {

        // Find the booking using its unique reference.
        Booking booking = bookingRepository
                .findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found"));

        // A ticket is valid only after payment confirmation.
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ticket is available only for confirmed bookings");
        }

        // Convert seat entities into readable labels such as A1 or B2.
        List<String> seatLabels = booking.getSeats().stream()
                .map(seat -> seat.getRowLabel() + seat.getSeatNumber())
                .sorted()
                .toList();

        // Generate a QR image using the booking reference.
        String qrCodeBase64 = generateQrCode(booking.getBookingCode());

        // Build the information the customer needs to see.
        return new TicketResponse(
                booking.getBookingCode(),
                booking.getEvent().getName(),
                booking.getEvent().getVenue(),
                booking.getEvent().getEventDateTime(),
                booking.getStatus(),
                booking.getTotalAmount(),
                seatLabels,
                booking.isCheckedIn(),
                qrCodeBase64
        );
    }

    // Generate a PNG QR code and encode its bytes as Base64.
    private String generateQrCode(String bookingCode) {

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            // The QR contains only the unique booking reference.
            BitMatrix matrix = qrCodeWriter.encode(
                    bookingCode,
                    BarcodeFormat.QR_CODE,
                    250,
                    250
            );

            // Render the QR matrix as a PNG image.
            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            ImageIO.write(
                    MatrixToImageWriter.toBufferedImage(matrix),
                    "PNG",
                    outputStream
            );

            // Base64 makes the image easy to send in a JSON response.
            return Base64.getEncoder()
                    .encodeToString(outputStream.toByteArray());

        } catch (WriterException | IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not generate ticket QR code",
                    exception
            );
        }
    }
}
