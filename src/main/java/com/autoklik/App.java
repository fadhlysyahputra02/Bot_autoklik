package com.autoklik;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

public class App {

    static boolean running = false;
    static WebDriver driver = null;
    static volatile boolean manualRefresh = false;
    static volatile boolean waitForNext = false;
    
    // History management
    static final String HISTORY_FILE = "xpath_history.txt";
    static Set<String> urlHistory = new LinkedHashSet<>();
    static Set<String> xpath1History = new LinkedHashSet<>();
    static Set<String> xpath2History = new LinkedHashSet<>();
    static Set<String> xpath3History = new LinkedHashSet<>();
    
    static {
        loadHistory();
    }
    
    // Load history from file
    static void loadHistory() {
        try {
            Path path = Paths.get(HISTORY_FILE);
            if (Files.exists(path)) {
                List<String> lines = Files.readAllLines(path);
                for (String line : lines) {
                    if (line.startsWith("URL:")) {
                        urlHistory.add(line.substring(4));
                    } else if (line.startsWith("XPATH1:")) {
                        xpath1History.add(line.substring(7));
                    } else if (line.startsWith("XPATH2:")) {
                        xpath2History.add(line.substring(7));
                    } else if (line.startsWith("XPATH3:")) {
                        xpath3History.add(line.substring(7));
                    }
                }
            }
        } catch (Exception e) {
            // Ignore if file doesn't exist or can't be read
        }
    }
    
    // Save history to file
    static void saveHistory() {
        try {
            List<String> lines = new ArrayList<>();
            for (String url : urlHistory) {
                lines.add("URL:" + url);
            }
            for (String xpath : xpath1History) {
                lines.add("XPATH1:" + xpath);
            }
            for (String xpath : xpath2History) {
                lines.add("XPATH2:" + xpath);
            }
            for (String xpath : xpath3History) {
                lines.add("XPATH3:" + xpath);
            }
            Files.write(Paths.get(HISTORY_FILE), lines);
        } catch (Exception e) {
            // Ignore save errors
        }
    }

    static String normalizeUrl(String url) {
        if (url == null) {
            return "";
        }
        url = url.trim();
        if (url.isEmpty()) {
            return "";
        }
        if (!url.matches("^(?i:https?://).*")) {
            url = "http://" + url;
        }
        return url;
    }

    static String getComboBoxText(JComboBox<String> combo) {
        if (combo == null) {
            return "";
        }
        // Coba ambil dari editor component (yang lebih andal)
        try {
            javax.swing.JTextField editor = (javax.swing.JTextField) combo.getEditor().getEditorComponent();
            String editorText = editor.getText().trim();
            if (!editorText.isEmpty()) {
                return editorText;
            }
        } catch (Exception e) {
            // Fallback jika gagal
        }
        
        // Fallback ke selected item
        Object selected = combo.getSelectedItem();
        return selected == null ? "" : selected.toString().trim();
    }

    public static void main(String[] args) {

        // ✅ Look & Feel modern
        try {
            UIManager.setLookAndFeel(
                    "javax.swing.plaf.nimbus.NimbusLookAndFeel"
            );
        } catch (Exception ignored) {}

        JFrame frame = new JFrame("State Auto Clicker");
        frame.setSize(650, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        frame.setContentPane(mainPanel);

        // ===== FORM PANEL =====
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 8));

        JComboBox<String> urlField = new JComboBox<>(urlHistory.toArray(new String[0]));
        urlField.setEditable(true);
        urlField.setSelectedItem("");
        JComboBox<String> xpathField = new JComboBox<>(xpath1History.toArray(new String[0]));
        xpathField.setEditable(true);
        xpathField.setSelectedItem("");
        JComboBox<String> xpath2Field = new JComboBox<>(xpath2History.toArray(new String[0]));
        xpath2Field.setEditable(true);
        xpath2Field.setSelectedItem("");
        
        JComboBox<String> xpath3Field = new JComboBox<>(xpath3History.toArray(new String[0]));
        xpath3Field.setEditable(true);
        xpath3Field.setSelectedItem("");
        JTextField quantityField = new JTextField("1");
        JTextField startTimeField = new JTextField("10:00:00");
        JTextField chromeProfilePathField = new JTextField(System.getProperty("user.home") + "/Library/Application Support/Google/Chrome");
        JTextField chromeProfileDirField = new JTextField("Default");

        // Enable copy and paste for all fields
        KeyAdapter copyPasteAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                boolean isCtrlOrCmd = (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0 ||
                        (e.getModifiersEx() & KeyEvent.META_DOWN_MASK) != 0;
                if (isCtrlOrCmd) {
                    if (e.getKeyCode() == KeyEvent.VK_C) {
                        // Copy
                        try {
                            //coba
                            String selectedText = "";
                            if (e.getSource() instanceof javax.swing.JTextField) {
                                javax.swing.JTextField field = (javax.swing.JTextField) e.getSource();
                                selectedText = field.getSelectedText();
                            } else if (e.getSource() instanceof javax.swing.text.JTextComponent) {
                                javax.swing.text.JTextComponent comp = (javax.swing.text.JTextComponent) e.getSource();
                                selectedText = comp.getSelectedText();
                            }
                            if (!selectedText.isEmpty()) {
                                java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(selectedText);
                                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                            }
                        } catch (Exception ex) {
                            // Ignore copy errors
                        }
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_V) {
                        // Paste
                        try {
                            java.awt.datatransfer.Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
                            java.awt.datatransfer.Transferable contents = clipboard.getContents(null);
                            if (contents != null && contents.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.stringFlavor)) {
                                String pasteText = (String) contents.getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor);
                                if (e.getSource() instanceof javax.swing.JTextField) {
                                    javax.swing.JTextField field = (javax.swing.JTextField) e.getSource();
                                    field.replaceSelection(pasteText);
                                } else if (e.getSource() instanceof javax.swing.text.JTextComponent) {
                                    javax.swing.text.JTextComponent comp = (javax.swing.text.JTextComponent) e.getSource();
                                    comp.replaceSelection(pasteText);
                                }
                            }
                        } catch (Exception ex) {
                            // Ignore paste errors
                        }
                        e.consume();
                    }
                }
            }
        };

        urlField.getEditor().getEditorComponent().addKeyListener(copyPasteAdapter);
        xpathField.getEditor().getEditorComponent().addKeyListener(copyPasteAdapter);
        xpath2Field.getEditor().getEditorComponent().addKeyListener(copyPasteAdapter);
        xpath3Field.getEditor().getEditorComponent().addKeyListener(copyPasteAdapter);
        quantityField.addKeyListener(copyPasteAdapter);
        startTimeField.addKeyListener(copyPasteAdapter);

        formPanel.add(new JLabel("URL"));
        formPanel.add(urlField);
        formPanel.add(new JLabel("FULL XPATH"));
        formPanel.add(xpathField);
        formPanel.add(new JLabel("XPATH ke-2 (Qty)"));
        formPanel.add(xpath2Field);
        formPanel.add(new JLabel("XPATH ke-3 (Buy)"));
        formPanel.add(xpath3Field);
        formPanel.add(new JLabel("Jumlah"));
        formPanel.add(quantityField);
        formPanel.add(new JLabel("Jam (HH:mm:ss)"));
        formPanel.add(startTimeField);
        formPanel.add(new JLabel("Chrome profile path (optional)"));
        formPanel.add(chromeProfilePathField);
        formPanel.add(new JLabel("Chrome profile directory"));
        formPanel.add(chromeProfileDirField);

        // ===== BUTTONS PANEL =====
        JPanel buttonPanel = new JPanel();
        JButton startBtn = new JButton("START");
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        startBtn.setPreferredSize(new Dimension(120, 35));

        JButton restartBtn = new JButton("RESTART");
        restartBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        restartBtn.setPreferredSize(new Dimension(120, 35));

        JButton nextBtn = new JButton("NEXT");
        nextBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nextBtn.setPreferredSize(new Dimension(120, 35));

        buttonPanel.add(startBtn);
        buttonPanel.add(restartBtn);
        buttonPanel.add(nextBtn);

        // ===== LOG PANEL =====
        JTextArea logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setEnabled(true);
        logArea.setBackground(UIManager.getColor("TextField.background"));
        logArea.setForeground(UIManager.getColor("TextField.foreground"));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(
                BorderFactory.createTitledBorder("📋 Log Activity")
        );

        // ===== LAYOUT =====
        JPanel formWithButtons = new JPanel(new BorderLayout(10, 10));
        formWithButtons.add(formPanel, BorderLayout.CENTER);
        formWithButtons.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(formWithButtons, BorderLayout.NORTH);
        mainPanel.add(logScroll, BorderLayout.CENTER);

        // ===== ACTION =====
        startBtn.addActionListener(e -> {
            running = true;
            startBtn.setEnabled(false);

            new Thread(() -> {
                try {
                    logArea.append("Starting START process...\n");
                    String rawUrl = getComboBoxText(urlField);
                    logArea.append("Raw URL input: '" + rawUrl + "'\n");
                    String url = normalizeUrl(rawUrl);
                    logArea.append("Normalized URL: '" + url + "'\n");
                    String xpath = getComboBoxText(xpathField);
                    String xpath2 = getComboBoxText(xpath2Field);
                    String xpath3 = getComboBoxText(xpath3Field);
                    String quantity = quantityField.getText().trim();
                    
                    if (url.isEmpty()) {
                        logArea.append("ERROR: URL tidak boleh kosong\n");
                        return;
                    }

                    // Save to history
                    if (!rawUrl.isEmpty()) urlHistory.add(rawUrl);
                    if (!xpath.isEmpty()) xpath1History.add(xpath);
                    if (!xpath2.isEmpty()) xpath2History.add(xpath2);
                    if (!xpath3.isEmpty()) xpath3History.add(xpath3);
                    saveHistory();
                    
                    if (xpath.isEmpty()) {
                        logArea.append("ERROR: FULL XPATH tidak boleh kosong\n");
                        return;
                    }
                    // XPATH ke-2, ke-3, dan quantity sekarang opsional
                    String finalQuantity = quantity.isEmpty() ? "1" : quantity;

                    ZoneId jakartaZone = ZoneId.of("Asia/Jakarta");
                    LocalTime targetTime =
                            LocalTime.parse(startTimeField.getText());

                    ZonedDateTime now =
                            ZonedDateTime.now(jakartaZone);

                    ZonedDateTime targetDateTime =
                            now.with(targetTime);

                    if (targetDateTime.isBefore(now)) {
                        targetDateTime = targetDateTime.plusDays(1);
                    }

                    String chromeProfilePath = chromeProfilePathField.getText().trim();
                    String chromeProfileDir = chromeProfileDirField.getText().trim();
                    ChromeOptions options = new ChromeOptions();
                    options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
                    options.setExperimentalOption("useAutomationExtension", false);
                    options.addArguments("--disable-blink-features=AutomationControlled");
                    options.addArguments("--disable-infobars");
                    options.addArguments("--disable-extensions");
                    if (!chromeProfilePath.isEmpty()) {
                        options.addArguments("--user-data-dir=" + chromeProfilePath);
                        if (!chromeProfileDir.isEmpty()) {
                            options.addArguments("--profile-directory=" + chromeProfileDir);
                        }
                        logArea.append("Using Chrome profile path: " + chromeProfilePath + "\n");
                        logArea.append("Using profile directory: " + chromeProfileDir + "\n");
                    } else {
                        logArea.append("Using default Chrome profile settings\n");
                    }

                    driver = new ChromeDriver(options);
                    logArea.append("ChromeDriver created successfully\n");
                    try {
                        logArea.append("Attempting to navigate to: '" + url + "'\n");
                        driver.get(url);
                        logArea.append("Chrome opened and URL loaded: " + url + "\n");
                        logArea.append("Current URL in browser: " + driver.getCurrentUrl() + "\n");
                    } catch (Exception navigationError) {
                        logArea.append("ERROR: driver.get() failed: " + navigationError.getMessage() + "\n");
                        logArea.append("Retrying with navigate().to(url)\n");
                        try {
                            driver.navigate().to(url);
                            logArea.append("Chrome loaded URL with navigate().to: " + url + "\n");
                            logArea.append("Current URL after navigate: " + driver.getCurrentUrl() + "\n");
                        } catch (Exception retryError) {
                            logArea.append("ERROR: navigate().to() also failed: " + retryError.getMessage() + "\n");
                        }
                    }
                    logArea.append("Waiting until: " + targetDateTime + "\n");

                    while (running) {
                        ZonedDateTime current = ZonedDateTime.now(jakartaZone);
                        ZonedDateTime rapidReloadStart = targetDateTime.minusSeconds(3);

                        if (current.isBefore(rapidReloadStart)) {
                            long sleepMillis = Duration.between(current, rapidReloadStart).toMillis();
                            Thread.sleep(Math.min(sleepMillis, 1000));
                            continue;
                        }

                        if (current.isBefore(targetDateTime)) {
                            logArea.append("Rapid reload mode for 3 seconds before target\n");
                            while (ZonedDateTime.now(jakartaZone).isBefore(targetDateTime)) {
                                driver.navigate().refresh();
                                Thread.sleep(300);
                            }
                        }

                        logArea.append("START PROCESS\n");
                        try {
                            // Klik XPATH awal
                            try {
                                logArea.append("Looking for FULL XPATH: " + xpath + "\n");
                                WebElement element = driver.findElement(By.xpath(xpath));
                                logArea.append("FULL XPATH FOUND → WAITING FOR ENABLED\n");
                                
                                // Tunggu sampai element enabled, dengan refresh setiap 3 detik
                                long startWait = System.currentTimeMillis();
                                long timeoutMs = 300000; // 5 menit timeout
                                boolean isEnabled = false;
                                
                                while (!isEnabled && (System.currentTimeMillis() - startWait) < timeoutMs) {
                                    try {
                                        element = driver.findElement(By.xpath(xpath)); // Refresh reference
                                        if (element.isDisplayed() && element.isEnabled()) {
                                            isEnabled = true;
                                            logArea.append("FULL XPATH ENABLED → CLICK\n");
                                            element.click();
                                            logArea.append("FULL XPATH CLICKED SUCCESSFULLY\n");
                                            logArea.append("Waiting 5 seconds for manual robot verification...\n");
                                            Thread.sleep(5000);
                                            logArea.append("Click NEXT button to continue to XPath ke-2\n");
                                            waitForNext = true;
                                            while (waitForNext && running) {
                                                Thread.sleep(100);
                                            }
                                            if (!running) return;
                                        } else {
                                            logArea.append("Element not enabled yet (displayed: " + element.isDisplayed() + ", enabled: " + element.isEnabled() + ") - waiting for NEXT button\n");
                                            manualRefresh = false;
                                            while (!manualRefresh && running) {
                                                Thread.sleep(100); // Wait for NEXT button
                                            }
                                            if (!running) break;
                                            driver.navigate().refresh();
                                        }
                                    } catch (Exception ex) {
                                        logArea.append("Error during wait: " + ex.getMessage() + " - waiting for NEXT button\n");
                                        manualRefresh = false;
                                        while (!manualRefresh && running) {
                                            Thread.sleep(100); // Wait for NEXT button
                                        }
                                        if (!running) break;
                                        driver.navigate().refresh();
                                    }
                                }
                                
                                if (!isEnabled) {
                                    logArea.append("ERROR: FULL XPATH not enabled within timeout\n");
                                    return;
                                }
                            } catch (Exception ex) {
                                logArea.append("ERROR: FULL XPATH click failed: " + ex.getMessage() + "\n");
                                logArea.append("Stack trace: " + java.util.Arrays.toString(ex.getStackTrace()) + "\n");
                                return;
                            }
                            
                            // Tunggu halaman baru dan deteksi XPATH ke-2 (quantity field) - OPSIONAL
                            if (!xpath2.isEmpty()) {
                                logArea.append("Waiting for page change and detecting XPATH ke-2 (quantity)...\n");
                                long startTime = System.currentTimeMillis();
                                boolean xpath2Found = false;
                                while (!xpath2Found && (System.currentTimeMillis() - startTime) < 300000) { // 5 menit timeout
                                    try {
                                        WebElement quantityElement = driver.findElement(By.xpath(xpath2));
                                        logArea.append("XPATH ke-2 FOUND → SET QUANTITY: " + finalQuantity + "\n");
                                        try {
                                            quantityElement.clear();
                                            quantityElement.sendKeys(finalQuantity);
                                            logArea.append("Quantity set as input field\n");
                                        } catch (Exception e1) {
                                            // Mungkin dropdown, coba select
                                            try {
                                                Select select = new Select(quantityElement);
                                                boolean found = false;
                                                
                                                // 1. Coba by exact visible text
                                                try {
                                                    select.selectByVisibleText(finalQuantity);
                                                    logArea.append("✓ Selected by exact text: " + finalQuantity + "\n");
                                                    found = true;
                                                } catch (Exception e3a) {
                                                    logArea.append("Exact match not found, trying alternatives...\n");
                                                }
                                                
                                                // 2. Jika tidak ketemu, coba by value attribute
                                                if (!found) {
                                                    try {
                                                        select.selectByValue(finalQuantity);
                                                        logArea.append("✓ Selected by value: " + finalQuantity + "\n");
                                                        found = true;
                                                    } catch (Exception e3b) {}
                                                }
                                                
                                                // 3. Coba partial match atau containing
                                                if (!found) {
                                                    java.util.List<WebElement> selectOptions = select.getOptions();
                                                    logArea.append("Available options: ");
                                                    for (WebElement option : selectOptions) {
                                                        String optText = option.getText().trim();
                                                        String optValue = option.getAttribute("value");
                                                        logArea.append("[" + optText + "|" + optValue + "] ");
                                                        
                                                        // Coba match dengan berbagai format
                                                        if (optText.contains(finalQuantity) || optText.equals(finalQuantity) || 
                                                            optValue.equals(finalQuantity) || optValue.contains(finalQuantity)) {
                                                            select.selectByValue(optValue.isEmpty() ? optText : optValue);
                                                            logArea.append("\n✓ Selected by pattern match: " + optText + "\n");
                                                            found = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                                
                                                if (found) {
                                                    logArea.append("Quantity set as dropdown select\n");
                                                } else {
                                                    logArea.append("ERROR: Cannot find matching option for quantity: " + finalQuantity + "\n");
                                                }
                                            } catch (Exception e2) {
                                                logArea.append("Failed to set quantity: " + e2.getMessage() + "\n");
                                            }
                                        }
                                        xpath2Found = true;
                                    } catch (Exception ex) {
                                        logArea.append("XPATH ke-2 not found yet, refresh and retry...\n");
                                        Thread.sleep(2000);
                                        driver.navigate().refresh();
                                    }
                                }
                                
                                if (!xpath2Found) {
                                    logArea.append("XPATH ke-2 not found within timeout, skipping quantity setting\n");
                                }
                            } else {
                                logArea.append("XPATH ke-2 not provided, skipping quantity setting\n");
                            }
                            
                            // Klik XPATH ke-3 (tombol order/buy) - OPSIONAL
                            if (!xpath3.isEmpty()) {
                                Thread.sleep(2000);
                                try {
                                    WebElement buyElement = driver.findElement(By.xpath(xpath3));
                                    logArea.append("XPATH ke-3 FOUND → CLICK BUY\n");
                                    buyElement.click();
                                    logArea.append("BUY BUTTON CLICKED SUCCESSFULLY\n");
                                } catch (Exception ex) {
                                    logArea.append("ERROR: XPATH ke-3 click failed: " + ex.getMessage() + "\n");
                                }
                            } else {
                                logArea.append("XPATH ke-3 not provided, skipping buy button click\n");
                            }
                            
                            running = false;
                            logArea.append("PROCESS COMPLETED\n");
                        } catch (Exception ex) {
                            logArea.append("Unexpected error: " + ex.getMessage() + "\n");
                        }
                    }

                } catch (Exception ex) {
                    logArea.append("ERROR: " + ex.getMessage() + "\n");
                } finally {
                    startBtn.setEnabled(true);
                }
            }).start();
        });

        restartBtn.addActionListener(e -> {
            running = false;
            if (driver != null) {
                driver.quit();
                driver = null;
                logArea.append("Chrome closed\n");
            }
            logArea.append("Restarting application...\n");
            frame.dispose();
            App.main(new String[0]);
        });

        nextBtn.addActionListener(e -> {
            manualRefresh = true;
            waitForNext = false;
            logArea.append("NEXT button clicked - continuing process\n");
        });

        frame.setVisible(true);
    }
}
