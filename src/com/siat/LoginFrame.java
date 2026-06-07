package com.siat;

import com.siat.dao.UsuarioDAO;
import com.siat.models.Usuario;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new RoundTextField(18);
    private final JPasswordField passwordField = new RoundPasswordField(18);
    private final JLabel messageLabel = new JLabel(" ");

    public LoginFrame() {
        setTitle("SIAT - Inicio de Sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(440, 460);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel principal con degradado
        GradientPanel mainPanel = new GradientPanel(new GridBagLayout());
        
        // Tarjeta del formulario con bordes redondeados
        RoundPanel card = new RoundPanel();
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        card.setPreferredSize(new Dimension(350, 390));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // 1. Icono de Usuario Vectorial
        UserIcon userIcon = new UserIcon();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        card.add(userIcon, gbc);

        // 2. Título de la tarjeta
        JLabel titleLabel = new JLabel("INICIAR SESIÓN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 18, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(titleLabel, gbc);

        // 3. Etiqueta de Usuario
        JLabel userLabel = new JLabel("Usuario");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        userLabel.setForeground(new Color(165, 180, 252)); // Índigo suave
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(userLabel, gbc);

        // 4. Campo de Entrada de Usuario
        usernameField.setPreferredSize(new Dimension(0, 32));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 12, 0);
        card.add(usernameField, gbc);

        // 5. Etiqueta de Contraseña
        JLabel passLabel = new JLabel("Contraseña");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        passLabel.setForeground(new Color(165, 180, 252));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(passLabel, gbc);

        // 6. Campo de Entrada de Contraseña
        passwordField.setPreferredSize(new Dimension(0, 32));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(passwordField, gbc);

        // 7. Mensaje de Error
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        messageLabel.setForeground(new Color(239, 68, 68)); // Rojo vibrante
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 12, 0);
        card.add(messageLabel, gbc);

        // 8. Botón de Ingreso
        ModernButton loginButton = new ModernButton("Ingresar");
        loginButton.setPreferredSize(new Dimension(0, 36));
        loginButton.addActionListener(this::onLogin);
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(loginButton, gbc);

        mainPanel.add(card);
        add(mainPanel);
    }

    private void onLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        Usuario user = UsuarioDAO.authenticate(username, password);
        if (user == null) {
            messageLabel.setText("Usuario o contraseña incorrecta.");
            com.siat.dao.AuditDAO.log(null, "LOGIN", "users", username, false, "Intento de inicio de sesión fallido para el usuario: " + username);
            return;
        }

        com.siat.dao.AuditDAO.log(user.getUserId(), "LOGIN", "users", String.valueOf(user.getUserId()), true, "Inicio de sesión exitoso");

        showWelcomeDialog(user);

        DashboardFrame dashboard = new DashboardFrame(user);
        dashboard.setVisible(true);
        dispose();
    }

    private void showWelcomeDialog(Usuario user) {
        // --- Diálogo personalizado con el diseño del sistema ---
        JDialog dialog = new JDialog(this, "Bienvenida", true);
        dialog.setUndecorated(true);
        dialog.setSize(420, 310);
        dialog.setLocationRelativeTo(this);

        // Panel principal con fondo oscuro y bordes redondeados
        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo oscuro del sistema
                g2.setColor(new Color(30, 30, 46));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                // Borde sutil
                g2.setColor(new Color(55, 55, 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // --- Icono de check con degradado índigo ---
        JComponent checkIcon = new JComponent() {
            { setPreferredSize(new Dimension(56, 56)); }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = 50;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                // Círculo con degradado índigo
                GradientPaint gp = new GradientPaint(x, y, new Color(99, 102, 241), x + size, y + size, new Color(79, 70, 229));
                g2.setPaint(gp);
                g2.fillOval(x, y, size, size);
                // Brillo exterior
                g2.setColor(new Color(255, 255, 255, 25));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(x, y, size, size);
                // Check blanco
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = x + size / 2;
                int cy = y + size / 2;
                g2.drawLine(cx - 10, cy, cx - 3, cy + 8);
                g2.drawLine(cx - 3, cy + 8, cx + 12, cy - 8);
                g2.dispose();
            }
        };
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(checkIcon, gbc);

        // --- Título de bienvenida ---
        JLabel titleLabel = new JLabel("¡Bienvenido, " + user.getUsername() + "!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(titleLabel, gbc);

        // --- Subtítulo ---
        JLabel subtitleLabel = new JLabel("Sistema SIAT", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(165, 180, 252));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 16, 0);
        mainPanel.add(subtitleLabel, gbc);

        // --- Mensaje de auditoría con estilo ---
        JPanel warningPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 20, 32));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(new Color(99, 102, 241, 80));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        warningPanel.setOpaque(false);
        warningPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JLabel warningIcon = new JLabel("⚠️ ");
        warningIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        warningPanel.add(warningIcon, BorderLayout.WEST);

        JLabel warningText = new JLabel("<html><body style='width:260px; color:#a5b4fc; font-family:Segoe UI; font-size:10px;'>"
                + "Este sistema auditará y registrará toda la actividad realizada. "
                + "Todas las acciones serán monitoreadas para garantizar la seguridad y trazabilidad."
                + "</body></html>");
        warningPanel.add(warningText, BorderLayout.CENTER);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 20, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(warningPanel, gbc);

        // --- Botón "Continuar" con degradado índigo ---
        ModernButton continueBtn = new ModernButton("Continuar");
        continueBtn.setPreferredSize(new Dimension(0, 36));
        continueBtn.addActionListener(e -> dialog.dispose());
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 60, 0, 60);
        mainPanel.add(continueBtn, gbc);

        // Fondo del diálogo transparente para ver los bordes redondeados
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.getContentPane().setBackground(new Color(0, 0, 0, 0));
        if (dialog.getRootPane() != null) {
            dialog.getRootPane().setOpaque(false);
            dialog.getRootPane().putClientProperty("apple.awt.draggable", true);
        }
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(mainPanel, BorderLayout.CENTER);

        dialog.setVisible(true);
    }
}

// -------------------------------------------------------------
// COMPONENTES PERSONALIZADOS PARA DISEÑO PREMIUM
// -------------------------------------------------------------

class GradientPanel extends JPanel {
    private final Color startColor = new Color(15, 15, 26);
    private final Color endColor = new Color(30, 30, 50);

    public GradientPanel(LayoutManager layout) {
        super(layout);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        GradientPaint gp = new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        g2.dispose();
    }
}

class RoundPanel extends JPanel {
    private final int round = 20;
    private final Color bgColor = new Color(30, 30, 46);
    private final Color borderColor = new Color(55, 55, 80);

    public RoundPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, round, round);
        
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, round, round);
        
        g2.dispose();
    }
}

class UserIcon extends JComponent {
    public UserIcon() {
        setPreferredSize(new Dimension(72, 72));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int size = Math.min(w, h) - 6;
        int x = (w - size) / 2;
        int y = (h - size) / 2;

        // Círculo de fondo con degradado índigo
        GradientPaint gp = new GradientPaint(x, y, new Color(99, 102, 241), x + size, y + size, new Color(79, 70, 229));
        g2.setPaint(gp);
        g2.fillOval(x, y, size, size);

        // Borde exterior suave de brillo
        g2.setColor(new Color(255, 255, 255, 30));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(x, y, size, size);

        // Silueta blanca del usuario
        g2.setColor(Color.WHITE);
        
        // Cabeza
        int headSize = size / 3;
        int headX = x + (size - headSize) / 2;
        int headY = y + size / 4;
        g2.fillOval(headX, headY, headSize, headSize);

        // Hombros / Cuerpo
        int bodyW = (int) (size * 0.58);
        int bodyH = (int) (size * 0.32);
        int bodyX = x + (size - bodyW) / 2;
        int bodyY = headY + headSize + 3;
        g2.fillArc(bodyX, bodyY, bodyW, bodyH * 2, 0, 180);

        g2.dispose();
    }
}

class RoundTextField extends JTextField {
    private final int round = 10;
    private final Color bgColor = new Color(20, 20, 32);
    private final Color borderColor = new Color(55, 55, 80);
    private final Color focusColor = new Color(99, 102, 241);
    private boolean isFocused = false;

    public RoundTextField(int columns) {
        super(columns);
        setOpaque(false);
        setBackground(bgColor);
        setForeground(Color.WHITE);
        setCaretColor(Color.WHITE);
        setFont(new Font("Segoe UI", Font.PLAIN, 13));
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        
        addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                isFocused = true;
                repaint();
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, round, round);
        
        g2.setColor(isFocused ? focusColor : borderColor);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, round, round);
        
        g2.dispose();
        super.paintComponent(g);
    }
}

class RoundPasswordField extends JPasswordField {
    private final int round = 10;
    private final Color bgColor = new Color(20, 20, 32);
    private final Color borderColor = new Color(55, 55, 80);
    private final Color focusColor = new Color(99, 102, 241);
    private boolean isFocused = false;

    public RoundPasswordField(int columns) {
        super(columns);
        setOpaque(false);
        setBackground(bgColor);
        setForeground(Color.WHITE);
        setCaretColor(Color.WHITE);
        setFont(new Font("Segoe UI", Font.PLAIN, 13));
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        
        addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                isFocused = true;
                repaint();
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, round, round);
        
        g2.setColor(isFocused ? focusColor : borderColor);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, round, round);
        
        g2.dispose();
        super.paintComponent(g);
    }
}

class ModernButton extends JButton {
    private final int round = 10;
    private final Color startColor = new Color(99, 102, 241);
    private final Color endColor = new Color(79, 70, 229);
    private final Color hoverStart = new Color(129, 140, 248);
    private final Color hoverEnd = new Color(99, 102, 241);
    private boolean isHovered = false;

    public ModernButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                isHovered = true;
                repaint();
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Color s = isHovered ? hoverStart : startColor;
        Color e = isHovered ? hoverEnd : endColor;
        
        GradientPaint gp = new GradientPaint(0, 0, s, 0, getHeight(), e);
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), round, round);
        
        g2.dispose();
        super.paintComponent(g);
    }
}

class RoundFormattedTextField extends JFormattedTextField {
    private final int round = 10;
    private final Color bgColor = new Color(20, 20, 32);
    private final Color borderColor = new Color(55, 55, 80);
    private final Color focusColor = new Color(99, 102, 241);
    private boolean isFocused = false;

    public RoundFormattedTextField(AbstractFormatter formatter) {
        super(formatter);
        init();
    }

    public RoundFormattedTextField() {
        super();
        init();
    }

    private void init() {
        setOpaque(false);
        setBackground(bgColor);
        setForeground(Color.WHITE);
        setCaretColor(Color.WHITE);
        setFont(new Font("Segoe UI", Font.PLAIN, 13));
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        
        addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                isFocused = true;
                repaint();
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, round, round);
        
        g2.setColor(isFocused ? focusColor : borderColor);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, round, round);
        
        g2.dispose();
        super.paintComponent(g);
    }
}
