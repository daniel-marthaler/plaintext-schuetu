package ch.plaintext.schuetu.service.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * Service fuer QR-Code Generierung mittels ZXing
 */
@Service
@Slf4j
public class QrCodeService {

    /**
     * Generiert einen QR-Code als Base64-encoded PNG String
     *
     * @param content der Inhalt des QR-Codes (z.B. eine URL)
     * @param size    die Groesse in Pixel (Breite und Hoehe)
     * @return Base64-encoded PNG String
     */
    public String generateQrCodeBase64(String content, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.MARGIN, 1,
                    EncodeHintType.CHARACTER_SET, "UTF-8"
            );
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | IOException e) {
            log.error("Fehler beim Generieren des QR-Codes: {}", e.getMessage(), e);
            return "";
        }
    }
}
