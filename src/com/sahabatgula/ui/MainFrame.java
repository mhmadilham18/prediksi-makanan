package com.sahabatgula.ui;

import com.sahabatgula.model.ApiResponse;
import com.sahabatgula.model.Prediction;
import com.sahabatgula.service.ApiService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import javax.imageio.ImageIO;

public class MainFrame extends JFrame {

    private ImagePanel imagePanel;
    private JLabel resultLabel;
    private JButton selectButton;
    private JButton predictButton;
    private JProgressBar progressBar;

    private ApiService apiService;
    private File selectedFile;

    public MainFrame() {
        super("Pemindai Makanan - Sahabat Gula");
        apiService = new ApiService();
        initComponents();
        layoutComponents();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 700));
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // Panel gambar
        imagePanel = new ImagePanel();
        imagePanel.setBackground(new Color(245, 246, 250));

        // Hasil prediksi → JLabel narasi
        resultLabel = new JLabel("Hasil prediksi akan muncul di sini...");
        resultLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        resultLabel.setForeground(Color.DARK_GRAY);
        resultLabel.setVerticalAlignment(SwingConstants.TOP);
        resultLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        resultLabel.setOpaque(false);

        // Tombol
        selectButton = new JButton("Pilih Gambar");
        predictButton = new JButton("Prediksi");

        styleButton(selectButton, new Color(59, 130, 246)); // biru
        styleButton(predictButton, new Color(16, 185, 129)); // hijau
        predictButton.setEnabled(false);

        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        // Aksi
        selectButton.addActionListener(e -> selectImage());
        predictButton.addActionListener(e -> predictImage());
    }

    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(250, 250, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        // Card gambar
        JPanel imageCard = createCard("Preview Gambar", imagePanel);
        gbc.gridy = 0;
        gbc.weighty = 0.6;
        mainPanel.add(imageCard, gbc);

        // Card hasil
        JPanel resultCard = createCard("Hasil Prediksi", resultLabel);
        gbc.gridy = 1;
        gbc.weighty = 0.3;
        mainPanel.add(resultCard, gbc);

        // Panel tombol
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(selectButton);
        buttonPanel.add(predictButton);
        gbc.gridy = 2;
        gbc.weighty = 0.1;
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Progress bar bawah
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        bottomPanel.setBackground(new Color(250, 250, 250));
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createCard(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 223, 230), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.white);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        titleLabel.setBorder(new EmptyBorder(0, 0, 8, 0));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.white);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Gambar (JPG, PNG)", "jpg", "jpeg", "png"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage img = ImageIO.read(selectedFile);
                if (img == null) {
                    throw new IOException("Format gambar tidak didukung.");
                }
                imagePanel.setImage(img);
                predictButton.setEnabled(true);
                resultLabel.setText("Gambar siap diprediksi. Klik tombol 'Prediksi'.");
                resultLabel.setForeground(Color.DARK_GRAY);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Gagal memuat file gambar: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                selectedFile = null;
                predictButton.setEnabled(false);
            }
        }
    }

    private void predictImage() {
        if (selectedFile == null) return;

        progressBar.setVisible(true);
        predictButton.setEnabled(false);
        selectButton.setEnabled(false);
        resultLabel.setText("⏳ Sedang memproses...");
        resultLabel.setForeground(Color.DARK_GRAY);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<ApiResponse, Void>() {
            @Override
            protected ApiResponse doInBackground() throws Exception {
                return apiService.predictImage(selectedFile);
            }

            @Override
            protected void done() {
                try {
                    ApiResponse response = get();
                    Prediction best = response.getBest();
                    if (best != null) {
                        double acc = best.getConfidence() * 100;
                        String accuracyText = new DecimalFormat("#.##").format(acc) + "%";

                        if (acc >= 60) {
                            resultLabel.setForeground(new Color(34, 197, 94));
                            resultLabel.setText("<html>Model yakin makanan ini adalah <b>"
                                    + best.getName() + "</b> dengan akurasi " + accuracyText + ".</html>");
                        } else {
                            resultLabel.setForeground(new Color(239, 68, 68));
                            resultLabel.setText("<html>Model memperkirakan ini adalah <b>"
                                    + best.getName() + "</b> namun akurasinya rendah (" + accuracyText + ").</html>");
                        }
                    } else {
                        resultLabel.setForeground(Color.DARK_GRAY);
                        resultLabel.setText("Tidak ada hasil prediksi dari server.");
                    }
                } catch (Exception e) {
                    resultLabel.setForeground(new Color(239, 68, 68));
                    resultLabel.setText("Terjadi kesalahan saat memproses prediksi.");
                    JOptionPane.showMessageDialog(MainFrame.this, "Gagal terhubung ke server: " + e.getMessage(),
                            "Connection Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setVisible(false);
                    predictButton.setEnabled(true);
                    selectButton.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private static class ImagePanel extends JPanel {
        private BufferedImage image;

        public void setImage(BufferedImage image) {
            this.image = image;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int panelWidth = getWidth();
                int panelHeight = getHeight();
                int imgWidth = image.getWidth();
                int imgHeight = image.getHeight();

                double scale = Math.min((double) panelWidth / imgWidth, (double) panelHeight / imgHeight);
                int scaledWidth = (int) (imgWidth * scale);
                int scaledHeight = (int) (imgHeight * scale);

                int x = (panelWidth - scaledWidth) / 2;
                int y = (panelHeight - scaledHeight) / 2;

                g2d.drawImage(image, x, y, scaledWidth, scaledHeight, this);
                g2d.dispose();
            } else {
                String text = "Preview Gambar";
                g.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(text, x, y);
            }
        }
    }
}
