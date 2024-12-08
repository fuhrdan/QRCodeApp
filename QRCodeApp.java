import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.print.*;

public class QRCodeApp extends JFrame implements Printable {
    private JTextField urlField;
    private JLabel qrCodeLabel;
    private BufferedImage qrCodeImage;

    public QRCodeApp() {
        setTitle("QR Code Generator");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        JLabel urlLabel = new JLabel("Enter HTTPS Address:");
        urlField = new JTextField(20);
        JButton generateButton = new JButton("Generate QR Code");
        inputPanel.add(urlLabel);
        inputPanel.add(urlField);
        inputPanel.add(generateButton);

        // QR Code Display Panel
        qrCodeLabel = new JLabel("Your QR Code will appear here", SwingConstants.CENTER);
        qrCodeLabel.setPreferredSize(new Dimension(300, 300));
        qrCodeLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Print Button
        JButton printButton = new JButton("Print QR Code");
        printButton.addActionListener(e -> printQRCode());

        // Add listeners
        generateButton.addActionListener(new GenerateQRCodeListener());

        // Add components to the frame
        add(inputPanel, BorderLayout.NORTH);
        add(qrCodeLabel, BorderLayout.CENTER);
        add(printButton, BorderLayout.SOUTH);
    }

    // Listener to generate QR code
    private class GenerateQRCodeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String url = urlField.getText().trim();
            if (!url.startsWith("https://")) {
                JOptionPane.showMessageDialog(QRCodeApp.this,
                        "Please enter a valid HTTPS address.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                qrCodeImage = generateQRCodeImage(url);
                qrCodeLabel.setIcon(new ImageIcon(qrCodeImage));
            } catch (WriterException | IOException ex) {
                JOptionPane.showMessageDialog(QRCodeApp.this,
                        "Error generating QR Code: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Generate QR Code
    private BufferedImage generateQRCodeImage(String text) throws WriterException, IOException {
        int size = 300;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size);

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        return image;
    }

    // Print QR Code
    private void printQRCode() {
        if (qrCodeImage == null) {
            JOptionPane.showMessageDialog(this, "No QR Code to print. Please generate one first.", 
                                          "Print Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable(this);
        if (printerJob.printDialog()) {
            try {
                printerJob.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Error during printing: " + e.getMessage(), 
                                              "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        if (qrCodeImage != null) {
            g2d.drawImage(qrCodeImage, 0, 0, qrCodeImage.getWidth(), qrCodeImage.getHeight(), null);
        }
        return PAGE_EXISTS;
    }

    // Main method to run the app
    public static void main(String[] args) {
        // Ensure the ZXing library is available
        try {
            Class.forName("com.google.zxing.BarcodeFormat");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "ZXing library not found. Please include it in the project.", 
                                          "Library Missing", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        SwingUtilities.invokeLater(() -> {
            QRCodeApp app = new QRCodeApp();
            app.setVisible(true);
        });
    }
}
