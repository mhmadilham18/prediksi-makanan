package com.sahabatgula.ui;

import com.sahabatgula.model.ApiResponse;
import com.sahabatgula.model.Prediction;
import com.sahabatgula.service.ApiService;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainFrame extends JFrame {

    private final ApiService apiService;
    private ImagePanel imagePanel;
    private ResultPanel resultPanel;
    private JButton mainActionButton; 
    private File selectedFile;

    private static final String ACTION_SELECT = "Pilih Gambar";
    private static final String ACTION_PREDICT = "Prediksi";

    public MainFrame() {
        super("Pemindai Makanan - Sahabat Gula");
        apiService = new ApiService();
        initComponents();
        layoutComponents();
        initDragAndDrop();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(400, 700)); 
        setSize(400, 700); 
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        imagePanel = new ImagePanel();
        imagePanel.setOpaque(false); 

        resultPanel = new ResultPanel();
        resultPanel.showInitialMessage(); 

        mainActionButton = new JButton(ACTION_SELECT);
        styleButton(mainActionButton, new Color(239, 68, 68), new Color(220, 38, 38)); 
        mainActionButton.addActionListener(e -> {
            if (mainActionButton.getText().equals(ACTION_SELECT)) {
                selectImage();
            } else if (mainActionButton.getText().equals(ACTION_PREDICT)) {
                predictImage();
            }
        });
    }

    private void layoutComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(25, 25, 45)); 
        mainPanel.setBorder(new EmptyBorder(30, 20, 30, 20)); 

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false); 
        headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0)); 

        ImageIcon appLogoIcon = null;
        try {
            Image originalLogo = ImageIO.read(Objects.requireNonNull(getClass().getResource("/resource/icons/logo-gula.png")));
            Image scaledLogo = originalLogo.getScaledInstance(100, 100, Image.SCALE_SMOOTH); 
            appLogoIcon = new ImageIcon(scaledLogo);
        } catch (IOException | NullPointerException e) {
            System.err.println("Logo aplikasi tidak ditemukan di /resource/icons/logo-gula.png");
        }

        JLabel logoLabel = new JLabel(appLogoIcon);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appNameLabel = new JLabel("Sahabat Gula");
        appNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        appNameLabel.setForeground(Color.WHITE);
        appNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(logoLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(appNameLabel);

        mainPanel.add(headerPanel);

        JPanel imageContainer = new JPanel(new GridBagLayout()); 
        imageContainer.setOpaque(false);
        imageContainer.setBorder(new EmptyBorder(20, 0, 20, 0)); 
        imageContainer.add(imagePanel); 

        mainPanel.add(imageContainer);

        JPanel resultContainer = new JPanel(new GridBagLayout()); 
        resultContainer.setOpaque(false);
        resultContainer.setBorder(new EmptyBorder(20, 0, 40, 0)); 
        resultContainer.add(resultPanel);

        mainPanel.add(resultContainer);

        mainActionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setOpaque(false);
        buttonWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, mainActionButton.getPreferredSize().height));
        buttonWrapper.add(mainActionButton);
        
        mainPanel.add(buttonWrapper);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void initDragAndDrop() {
        new DropTarget(imagePanel, new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    @SuppressWarnings("unchecked")
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!droppedFiles.isEmpty()) {
                        handleFileSelection(droppedFiles.get(0));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Gagal menerima file: Pastikan Anda hanya menyeret file gambar.",
                            "Drag & Drop Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void styleButton(JButton button, Color bgColor, Color hoverColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.white);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40)); 
        button.setMaximumSize(new Dimension(250, button.getPreferredSize().height)); 
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Gambar (JPG, PNG)", "jpg", "jpeg", "png"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            handleFileSelection(fileChooser.getSelectedFile());
        }
    }

    private void handleFileSelection(File file) {
        selectedFile = file;
        try {
            BufferedImage img = ImageIO.read(selectedFile);
            if (img == null) throw new IOException("Format gambar tidak didukung.");
            imagePanel.setImage(img);
            mainActionButton.setText(ACTION_PREDICT); 
            resultPanel.showInitialMessage(); 
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat file gambar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            selectedFile = null;
            mainActionButton.setText(ACTION_SELECT); 
            imagePanel.clearImage(); 
        }
    }

    private void predictImage() {
        if (selectedFile == null) return;

        mainActionButton.setEnabled(false);
        resultPanel.showLoading();

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
                    Prediction bestPrediction = response.getBest();
                    if (bestPrediction != null) {
                        resultPanel.updatePrediction(bestPrediction); // Tampilkan hasil prediksi
                    } else {
                        resultPanel.showError("Tidak ada hasil prediksi yang valid.");
                    }
                } catch (Exception e) {
                    resultPanel.showError("Gagal terhubung atau memproses prediksi.");
                    e.printStackTrace();
                } finally {
                    mainActionButton.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                    mainActionButton.setText(ACTION_SELECT);
                }
            }
        }.execute();
    }

    private static class ResultPanel extends JPanel {
        private final JLabel predictionLabel; 
        private final JLabel statusLabel;
        private final JPanel mainResultPanel;

        public ResultPanel() {
            setLayout(new GridBagLayout()); 
            setOpaque(false);

            mainResultPanel = new JPanel();
            mainResultPanel.setLayout(new BoxLayout(mainResultPanel, BoxLayout.Y_AXIS));
            mainResultPanel.setOpaque(false);
            
            predictionLabel = new JLabel();
            predictionLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            predictionLabel.setForeground(Color.WHITE); 
            predictionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            predictionLabel.setHorizontalAlignment(SwingConstants.CENTER); 

            mainResultPanel.add(predictionLabel);

            statusLabel = new JLabel("", SwingConstants.CENTER); 
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            statusLabel.setForeground(new Color(200, 200, 200)); 

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(statusLabel, gbc);
            add(mainResultPanel, gbc); 

            mainResultPanel.setVisible(false);
        }

        public void showInitialMessage() {
            predictionLabel.setText(""); 
            statusLabel.setText("Hasil prediksi akan muncul di sini.");
            statusLabel.setForeground(new Color(200, 200, 200));
            statusLabel.setVisible(true);
            mainResultPanel.setVisible(false);
            revalidate();
            repaint();
        }

        public void showLoading() {
            predictionLabel.setText(""); 
            statusLabel.setText("Menganalisis gambar...");
            statusLabel.setForeground(new Color(200, 200, 200));
            statusLabel.setVisible(true);
            mainResultPanel.setVisible(false);
            revalidate();
            repaint();
        }

        public void showError(String message) {
            predictionLabel.setText(""); 
            statusLabel.setText("<html><div style='text-align: center;'>" + message + "</div></html>");
            statusLabel.setForeground(new Color(239, 68, 68));
            statusLabel.setVisible(true);
            mainResultPanel.setVisible(false);
            revalidate();
            repaint();
        }

        public void updatePrediction(Prediction prediction) {
            if (prediction == null) {
                showError("Tidak ada hasil prediksi yang valid.");
                return;
            }

            double confidence = prediction.getConfidence() * 100;
            String confidenceText = new DecimalFormat("#.##").format(confidence) + "% ";

            String foodName = prediction.getName();
            String htmlText = "<html><center>" +
                            "<span style='color: #EF4444;'>" + foodName + "</span>" +           
                            "<br>" +
                            "<span style='font-size: 20pt; color: #10B981;'>" + confidenceText + "</span>" +  // Confidence dibuat berwarna HIJAU
                            "Confidence" + "</center></html>";

            predictionLabel.setText(htmlText);

            statusLabel.setVisible(false);
            mainResultPanel.setVisible(true);
            revalidate();
            repaint();
        }
    }

    private static class ImagePanel extends JPanel {
        private BufferedImage image;
        private Image placeholderIcon; 

        public ImagePanel() {
            setPreferredSize(new Dimension(200, 200)); 
            setMinimumSize(new Dimension(200, 200));
            setMaximumSize(new Dimension(200, 200));
            setOpaque(false);

            try {
                placeholderIcon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/resource/icons/image-holder.png")));
            } catch (IOException | NullPointerException e) {
                System.err.println("Placeholder image-holder.png tidak ditemukan.");
                placeholderIcon = null;
            }
        }

        public void setImage(BufferedImage image) {
            this.image = image;
            repaint();
        }

        public void clearImage() {
            this.image = null;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // Gambar lingkaran background
            g2d.setColor(new Color(40, 40, 60)); // Warna abu-abu gelap untuk lingkaran background
            g2d.fillOval(0, 0, getWidth(), getHeight());

            if (image != null) {
                int diameter = Math.min(getWidth(), getHeight());
                BufferedImage roundImage = createRoundImage(image, diameter);
                g2d.drawImage(roundImage, 0, 0, null);
            } else {
                if (placeholderIcon != null) {
                    int iconWidth = placeholderIcon.getWidth(null);
                    int iconHeight = placeholderIcon.getHeight(null);
                    int maxDim = Math.min(getWidth(), getHeight());
                    double scale = Math.min((double) (maxDim - 40) / iconWidth, (double) (maxDim - 40) / iconHeight);
                    int scaledWidth = (int) (iconWidth * scale);
                    int scaledHeight = (int) (iconHeight * scale);
                    int x = (getWidth() - scaledWidth) / 2;
                    int y = (getHeight() - scaledHeight) / 2;
                    g2d.drawImage(placeholderIcon, x, y, scaledWidth, scaledHeight, this);
                }
            }
            g2d.dispose();
        }

        // Helper method untuk memotong gambar menjadi bulat
        private BufferedImage createRoundImage(BufferedImage src, int diameter) {
            BufferedImage round = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = round.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setClip(new Ellipse2D.Float(0, 0, diameter, diameter));
            // Hitung skala agar gambar memenuhi diameter
            double scale = Math.max((double) diameter / src.getWidth(), (double) diameter / src.getHeight());
            int scaledWidth = (int) (src.getWidth() * scale);
            int scaledHeight = (int) (src.getHeight() * scale);
            int x = (diameter - scaledWidth) / 2;
            int y = (diameter - scaledHeight) / 2;
            g2.drawImage(src, x, y, scaledWidth, scaledHeight, null);
            g2.dispose();
            return round;
        }
    }
}