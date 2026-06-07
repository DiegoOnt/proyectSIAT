package com.siat;

import com.siat.dao.ActivoDAO;
import com.siat.dao.AuditDAO;
import com.siat.models.Activo;
import com.siat.models.Audit;
import com.siat.models.Usuario;
import com.siat.services.InventoryService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import javax.swing.JFileChooser;
import com.siat.ExportUtil;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFrame extends JFrame implements AssetUpdateListener {
    private final Usuario currentUser;
    private final JTable assetTable = new JTable();
    private final JTable auditTable = new JTable();
    private final JLabel metricsLabel = new JLabel();

    // Referencias a las gráficas para permitir su actualización
    private ChartPanel categoryChart;
    private PieChartPanel costChart;
    private HorizontalBarChartPanel locationChart;

    public DashboardFrame(Usuario currentUser) {
        this.currentUser = currentUser;
        setTitle("SIAT - Panel de Control | " + currentUser.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        GradientPanel mainPanel = new GradientPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setContentPane(mainPanel);

        mainPanel.add(buildHeader(), BorderLayout.NORTH);
        
        Component centerComp;
        if (currentUser.getRoleLevel() >= 100) {
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.setBackground(new Color(30, 30, 46));
            tabbedPane.setForeground(Color.WHITE);
            tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            tabbedPane.addTab("Inventario", buildCenterPanel());
            tabbedPane.addTab("Auditorías", buildAuditPanel());
            
            centerComp = tabbedPane;
        } else {
            centerComp = buildCenterPanel();
        }
        mainPanel.add(centerComp, BorderLayout.CENTER);
        
        mainPanel.add(buildFooter(), BorderLayout.SOUTH);

        loadAssets();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Sistema de Inventario y Auditorías", SwingConstants.LEFT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(title, BorderLayout.WEST);

        JLabel userLabel = new JLabel("Usuario: " + currentUser.getUsername() + " - Rol: " + currentUser.getRoleName());
        userLabel.setForeground(new Color(165, 180, 252)); // Índigo suave
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.add(userLabel, BorderLayout.EAST);

        // Export buttons panel
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exportPanel.setOpaque(false);
        ModernButton exportPdfBtn = new ModernButton("Exportar PDF");
        exportPdfBtn.setPreferredSize(new Dimension(120, 32));
        exportPdfBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Guardar PDF");
            chooser.setSelectedFile(new File("assets_" + System.currentTimeMillis() + ".pdf"));
            int result = chooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    ExportUtil.exportTableToPdf(assetTable, file);
                    JOptionPane.showMessageDialog(this, "PDF exportado exitosamente.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al exportar PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        exportPanel.add(exportPdfBtn);
        ModernButton exportExcelBtn = new ModernButton("Exportar Excel");
        exportExcelBtn.setPreferredSize(new Dimension(120, 32));
        exportExcelBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Guardar Excel");
            chooser.setSelectedFile(new File("assets_" + System.currentTimeMillis() + ".xlsx"));
            int result = chooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    ExportUtil.exportTableToExcel(assetTable, file);
                    JOptionPane.showMessageDialog(this, "Excel exportado exitosamente.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al exportar Excel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        exportPanel.add(exportExcelBtn);
        header.add(exportPanel, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        // Panel de gráficas
        JPanel chartsPanel = new JPanel(new GridLayout(1, 3, 8, 8));
        chartsPanel.setOpaque(false);
        chartsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(55, 55, 80)),
            "Análisis", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12), Color.WHITE
        ));
        chartsPanel.setPreferredSize(new Dimension(0, 280));
        
        categoryChart = new ChartPanel("Cantidades por Categoría", getAssetsByCategory());
        costChart = new PieChartPanel("Costos por Categoría", getCostsByCategory());
        locationChart = new HorizontalBarChartPanel("Productos", getAlertsByLocation());
        
        chartsPanel.add(categoryChart);
        chartsPanel.add(costChart);
        chartsPanel.add(locationChart);
        panel.add(chartsPanel, BorderLayout.NORTH);

        // Panel de tabla
        assetTable.setFillsViewportHeight(true);
        assetTable.setRowHeight(28);
        styleTable(assetTable);
        assetTable.setDefaultRenderer(Object.class, new StatusCellRenderer());
        assetTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!currentUser.canEdit()) return;
                int row = assetTable.rowAtPoint(e.getPoint());
                int col = assetTable.columnAtPoint(e.getPoint());
                if (col == assetTable.getColumnCount() - 1 && row >= 0) {
                    handleRowAction(row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(assetTable);
        scrollPane.getViewport().setBackground(new Color(30, 30, 46));
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(55, 55, 80)),
            "Inventario (Click en Acciones para editar/eliminar)",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12), Color.WHITE
        ));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        if (currentUser.canEdit()) {
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            actionPanel.setOpaque(false);
            ModernButton addButton = new ModernButton("Agregar activo");
            addButton.setPreferredSize(new Dimension(140, 32));
            addButton.addActionListener(e -> {
                AddAssetFrame addFrame = new AddAssetFrame(currentUser, this);
                addFrame.setVisible(true);
            });
            actionPanel.add(addButton);
            panel.add(actionPanel, BorderLayout.SOUTH);
        }

        return panel;
    }

    private void styleTable(JTable table) {
        table.setBackground(new Color(30, 30, 46));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(55, 55, 80));
        table.setSelectionBackground(new Color(79, 70, 229));
        table.setSelectionForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        table.getTableHeader().setBackground(new Color(20, 20, 32));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(55, 55, 80)));
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        ModernButton logoutButton = new ModernButton("Cerrar sesión");
        logoutButton.setPreferredSize(new Dimension(120, 32));
        logoutButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        footer.add(logoutButton);
        return footer;
    }

    @Override
    public void onAssetSaved() {
        loadAssets();
    }

    @Override
    public void onAssetDeleted(int assetId) {
        loadAssets();
    }

    private void handleRowAction(int row) {
        if (!currentUser.canEdit()) {
            JOptionPane.showMessageDialog(this, "No tiene permisos para modificar activos.", "Permiso Denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sku = (String) assetTable.getValueAt(row, 0);
        int option = JOptionPane.showOptionDialog(this, "Selecciona una acción", "Opciones de Activo",
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
            new String[]{"Editar", "Eliminar", "Cancelar"}, "Cancelar");
        if (option == 0) {
            // Editar
            List<Activo> assets = ActivoDAO.findAll();
            Activo selectedAsset = assets.stream().filter(a -> a.getSku().equals(sku)).findFirst().orElse(null);
            if (selectedAsset != null) {
                EditAssetFrame editFrame = new EditAssetFrame(currentUser, selectedAsset, this);
                editFrame.setVisible(true);
            }
        } else if (option == 1) {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar activo " + sku + "?\nEsta acción borrará también el historial y alertas asociadas.", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = ActivoDAO.delete(sku);
                if (ok) {
                    com.siat.dao.AuditDAO.log(currentUser.getUserId(), "ELIMINAR_ACTIVO", "assets", sku, true, "Activo eliminado (SKU: " + sku + ")");
                    JOptionPane.showMessageDialog(this, "Activo " + sku + " eliminado correctamente.");
                    loadAssets(); // Actualiza la tabla y vuelve a renderizar los gráficos
                } else {
                    com.siat.dao.AuditDAO.log(currentUser.getUserId(), "ELIMINAR_ACTIVO", "assets", sku, false, "Fallo al eliminar activo (SKU: " + sku + ")");
                    JOptionPane.showMessageDialog(this, "Error al eliminar el activo de la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void loadAssets() {
        List<Activo> assets = ActivoDAO.findAll();
        metricsLabel.setText(InventoryService.buildSummary(assets));

        String[] columns;
        if (currentUser.canEdit()) {
            columns = new String[]{"SKU", "Nombre", "Categoría", "Ubicación", "Estado", "Etapa", "Ciclo", "Mantenimiento", "Cantidad", "Costo", "Alerta Ciclo Vida", "Acciones"};
        } else {
            columns = new String[]{"SKU", "Nombre", "Categoría", "Ubicación", "Estado", "Etapa", "Ciclo", "Mantenimiento", "Cantidad", "Costo", "Alerta Ciclo Vida"};
        }

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Activo asset : assets) {
            String lifecycleAlert = calculateLifecycleAlert(asset);
            Object[] rowData;
            if (currentUser.canEdit()) {
                rowData = new Object[]{
                    asset.getSku(),
                    asset.getName(),
                    asset.getCategory(),
                    asset.getLocation(),
                    asset.getStatus(),
                    asset.getLifecycleStage(),
                    asset.getLifecycleDays(),
                    asset.getMaintenance(),
                    asset.getQuantity(),
                    String.format("$%.2f", asset.getCost()),
                    lifecycleAlert,
                    "Editar/Eliminar"
                };
            } else {
                rowData = new Object[]{
                    asset.getSku(),
                    asset.getName(),
                    asset.getCategory(),
                    asset.getLocation(),
                    asset.getStatus(),
                    asset.getLifecycleStage(),
                    asset.getLifecycleDays(),
                    asset.getMaintenance(),
                    asset.getQuantity(),
                    String.format("$%.2f", asset.getCost()),
                    lifecycleAlert
                };
            }
            model.addRow(rowData);
        }

        assetTable.setModel(model);
        if (currentUser.canEdit()) {
            assetTable.getColumnModel().getColumn(assetTable.getColumnCount() - 1).setMaxWidth(100);
        }

        // ACTUALIZACIÓN DE LAS GRÁFICAS EN TIEMPO REAL
        if (categoryChart != null) categoryChart.updateData(getAssetsByCategory());
        if (costChart != null) costChart.updateData(getCostsByCategory());
        if (locationChart != null) {
            locationChart.updateData(getAlertsByLocation());
            locationChart.setLifecycleCounts(getLifecycleCountsByLocation());
        }
        
        if (currentUser.getRoleLevel() >= 100) {
            loadAudits();
        }
    }

    private JPanel buildAuditPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        auditTable.setFillsViewportHeight(true);
        auditTable.setRowHeight(28);
        styleTable(auditTable);
        
        auditTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(79, 70, 229));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(new Color(30, 30, 46));
                    c.setForeground(Color.WHITE);
                }
                
                if (column == 6 && value != null) {
                    String result = value.toString();
                    if ("FALLO".equalsIgnoreCase(result)) {
                        c.setBackground(new Color(185, 28, 28));
                        c.setForeground(Color.WHITE);
                    } else if ("ÉXITO".equalsIgnoreCase(result)) {
                        c.setBackground(new Color(22, 101, 52));
                        c.setForeground(Color.WHITE);
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(auditTable);
        scrollPane.getViewport().setBackground(new Color(30, 30, 46));
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(55, 55, 80)),
            "Historial de Auditorías del Sistema (Solo Lectura)",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12), Color.WHITE
        ));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadAudits() {
        if (currentUser.getRoleLevel() < 100) return;
        List<Audit> audits = AuditDAO.findAll();
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID Auditoría", "ID Usuario", "Usuario", "Acción", "Tabla Afectada", "ID Afectado", "Resultado", "Detalles", "Fecha/Hora", "Dirección IP"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Audit a : audits) {
            model.addRow(new Object[]{
                a.getAuditId(),
                a.getUserId() == null ? "N/A" : a.getUserId(),
                a.getUsername() == null ? "N/A" : a.getUsername(),
                a.getAction(),
                a.getTargetTable() == null ? "N/A" : a.getTargetTable(),
                a.getTargetId() == null ? "N/A" : a.getTargetId(),
                a.isSuccess() ? "ÉXITO" : "FALLO",
                a.getDetails() == null ? "" : a.getDetails(),
                a.getEventTime(),
                a.getIpAddress() == null ? "N/A" : a.getIpAddress()
            });
        }
        auditTable.setModel(model);
    }

    private Map<String, Integer> getAssetsByCategory() {
        List<Activo> assets = ActivoDAO.findAll();
        Map<String, Integer> categories = new HashMap<>();
        for (Activo a : assets) {
            categories.put(a.getCategory(), categories.getOrDefault(a.getCategory(), 0) + a.getQuantity());
        }
        return categories;
    }

    private String calculateLifecycleAlert(Activo asset) {
        try {
            if (asset.getPurchaseDate() == null || asset.getLifecycleDays() <= 0) {
                return "Desconocido";
            }
            
            java.time.LocalDate purchaseDate = java.time.LocalDate.parse(asset.getPurchaseDate());
            java.time.LocalDate expiryDate = purchaseDate.plusDays(asset.getLifecycleDays());
            java.time.LocalDate today = java.time.LocalDate.now();
            
            long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(today, expiryDate);
            
            if (daysUntilExpiry < 0) {
                return "Vencido";
            } else if (daysUntilExpiry <= 30) {
                return "Por vencer";
            } else {
                return "Vigente";
            }
        } catch (Exception e) {
            return "Desconocido";
        }
    }

    private Map<String, Integer> getCostsByCategory() {
        List<Activo> assets = ActivoDAO.findAll();
        Map<String, Integer> costs = new HashMap<>();
        for (Activo a : assets) {
            costs.put(a.getCategory(), costs.getOrDefault(a.getCategory(), 0) + (int) a.getCost());
        }
        return costs;
    }

    private Map<String, Integer> getAlertsByLocation() {
        List<Activo> assets = ActivoDAO.findAll();
        Map<String, Integer> alerts = new java.util.LinkedHashMap<>();
        alerts.put("Cuarto de Redes", 0);
        alerts.put("Sala de Servidores", 0);
        alerts.put("Almacén Central", 0);
        alerts.put("Bodega Alterna", 0);
        alerts.put("Centro de Acopio", 0);

        for (Activo a : assets) {
            String loc = a.getLocation();
            if (loc != null) {
                // Sumar alertas de la base de datos + alerta de ciclo de vida (Vencido o Por vencer)
                int totalAlerts = a.getUnresolvedAlerts();
                String lifecycleAlert = calculateLifecycleAlert(a);
                if ("Vencido".equals(lifecycleAlert) || "Por vencer".equals(lifecycleAlert)) {
                    totalAlerts++;
                }
                alerts.put(loc, alerts.getOrDefault(loc, 0) + totalAlerts);
            }
        }
        return alerts;
    }

    private Map<String, java.util.List<String[]>> getProductsByLocationWithLifecycle() {
        List<Activo> assets = ActivoDAO.findAll();
        Map<String, java.util.List<String[]>> productsByLocation = new java.util.LinkedHashMap<>();
        productsByLocation.put("Cuarto de Redes", new java.util.ArrayList<>());
        productsByLocation.put("Sala de Servidores", new java.util.ArrayList<>());
        productsByLocation.put("Almacén Central", new java.util.ArrayList<>());
        productsByLocation.put("Bodega Alterna", new java.util.ArrayList<>());
        productsByLocation.put("Centro de Acopio", new java.util.ArrayList<>());

        for (Activo a : assets) {
            String loc = a.getLocation();
            if (loc != null && productsByLocation.containsKey(loc)) {
                String lifecycleAlert = calculateLifecycleAlert(a);
                // Formato: [nombre, estado, color]
                String color;
                if ("Vencido".equals(lifecycleAlert)) {
                    color = "#B91C1C"; // Rojo
                } else if ("Por vencer".equals(lifecycleAlert)) {
                    color = "#D97706"; // Naranja
                } else {
                    color = "#166534"; // Verde
                }
                productsByLocation.get(loc).add(new String[]{a.getName(), lifecycleAlert, color});
            }
        }
        return productsByLocation;
    }

    private Map<String, int[]> getLifecycleCountsByLocation() {
        List<Activo> assets = ActivoDAO.findAll();
        Map<String, int[]> counts = new java.util.LinkedHashMap<>();
        counts.put("Cuarto de Redes", new int[]{0, 0, 0}); // [Vencido, Por vencer, Vigente]
        counts.put("Sala de Servidores", new int[]{0, 0, 0});
        counts.put("Almacén Central", new int[]{0, 0, 0});
        counts.put("Bodega Alterna", new int[]{0, 0, 0});
        counts.put("Centro de Acopio", new int[]{0, 0, 0});

        for (Activo a : assets) {
            String loc = a.getLocation();
            if (loc != null && counts.containsKey(loc)) {
                String lifecycleAlert = calculateLifecycleAlert(a);
                int[] locationCounts = counts.get(loc);
                if ("Vencido".equals(lifecycleAlert)) {
                    locationCounts[0]++;
                } else if ("Por vencer".equals(lifecycleAlert)) {
                    locationCounts[1]++;
                } else {
                    locationCounts[2]++;
                }
            }
        }
        return counts;
    }

    private static class ChartPanel extends JPanel {
        private final String title;
        private Map<String, Integer> data;

        public ChartPanel(String title, Map<String, Integer> data) {
            this.title = title;
            this.data = data;
            setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(55, 55, 80)),
                title, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Color.WHITE
            ));
            setMinimumSize(new Dimension(100, 250));
            setPreferredSize(new Dimension(0, 250));
            setBackground(new Color(30, 30, 46));
        }

        public void updateData(Map<String, Integer> newData) {
            this.data = newData;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) return;

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int numBars = data.size();
            int barWidth = (width - 60) / Math.max(numBars, 1);
            int chartHeight = height - 70;
            int maxValue = data.values().stream().max(Integer::compare).orElse(1);

            Color[] colors = {new Color(70, 130, 180), new Color(220, 20, 60), new Color(34, 139, 34), new Color(255, 165, 0)};
            int colorIdx = 0;
            int x = 30;

            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int barH = (int) ((entry.getValue() / (double) maxValue) * chartHeight);
                int barY = height - 40 - barH;

                // Barra
                g2d.setColor(colors[colorIdx % colors.length]);
                g2d.fillRect(x, barY, barWidth - 10, barH);
                g2d.setColor(new Color(55, 55, 80));
                g2d.drawRect(x, barY, barWidth - 10, barH);

                // Etiqueta valor encima
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g2d.drawString(String.valueOf(entry.getValue()), x + 5, barY - 5);

                // Etiqueta categoría abajo
                g2d.setColor(new Color(165, 180, 252));
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                String label = entry.getKey();
                if (label.length() > 10) label = label.substring(0, 10) + "...";
                g2d.drawString(label, x, height - 15);

                x += barWidth;
                colorIdx++;
            }
        }
    }

    private static class PieChartPanel extends JPanel {
        private final String title;
        private Map<String, Integer> data;

        public PieChartPanel(String title, Map<String, Integer> data) {
            this.title = title;
            this.data = data;
            setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(55, 55, 80)),
                title, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Color.WHITE
            ));
            setMinimumSize(new Dimension(100, 250));
            setPreferredSize(new Dimension(0, 250));
            setBackground(new Color(30, 30, 46));
        }

        public void updateData(Map<String, Integer> newData) {
            this.data = newData;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) return;

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int centerX = width / 2 - 45;
            int centerY = height / 2;
            int radius = Math.min(width, height) / 3;

            int total = data.values().stream().mapToInt(Integer::intValue).sum();
            if (total == 0) return;

            Color[] colors = {new Color(70, 130, 180), new Color(220, 20, 60), new Color(34, 139, 34), new Color(255, 165, 0), new Color(123, 104, 238)};
            int colorIdx = 0;
            double currentAngle = 0;

            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                double sliceAngle = (entry.getValue() * 360.0) / total;
                g2d.setColor(colors[colorIdx % colors.length]);
                g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, (int) currentAngle, (int) sliceAngle);
                g2d.setColor(new Color(30, 30, 46));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, (int) currentAngle, (int) sliceAngle);

                currentAngle += sliceAngle;
                colorIdx++;
            }

            // Leyenda
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            int legendX = centerX + radius + 15;
            int legendY = centerY - (data.size() * 10);
            colorIdx = 0;
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                g2d.setColor(colors[colorIdx % colors.length]);
                g2d.fillRect(legendX, legendY, 10, 10);
                g2d.setColor(Color.WHITE);
                String label = entry.getKey();
                if (label.length() > 12) label = label.substring(0, 12) + "...";
                g2d.drawString(label + " (" + entry.getValue() + ")", legendX + 15, legendY + 9);
                legendY += 12;
                colorIdx++;
            }
        }
    }

    private static class HorizontalBarChartPanel extends JPanel {
        private final String title;
        private Map<String, Integer> data;
        private Map<String, java.util.List<String[]>> productsWithLifecycle;
        private Map<String, int[]> lifecycleCounts;

        public HorizontalBarChartPanel(String title, Map<String, Integer> data) {
            this.title = title;
            this.data = data;
            this.productsWithLifecycle = null;
            this.lifecycleCounts = null;
            setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(55, 55, 80)),
                title, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Color.WHITE
            ));
            setMinimumSize(new Dimension(100, 250));
            setPreferredSize(new Dimension(0, 250));
            setBackground(new Color(30, 30, 46));
        }

        public void setLifecycleCounts(Map<String, int[]> lifecycleCounts) {
            this.lifecycleCounts = lifecycleCounts;
            repaint();
        }

        public void updateData(Map<String, Integer> newData) {
            this.data = newData;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Si hay información de conteos de ciclo de vida, mostrar barras apiladas
            if (lifecycleCounts != null && !lifecycleCounts.isEmpty()) {
                int topMargin = 35;
                int leftMargin = 100;
                int rightMargin = 50;
                int bottomMargin = 20;
                int usableHeight = height - topMargin - bottomMargin;
                int chartWidth = width - leftMargin - rightMargin;

                int numLocations = lifecycleCounts.size();
                int barHeight = usableHeight / Math.max(numLocations, 1);
                int barSpacing = 8;
                int actualBarHeight = Math.max(barHeight - barSpacing, 12);

                // Calcular valor máximo para escala
                int maxTotal = 0;
                for (int[] counts : lifecycleCounts.values()) {
                    int total = counts[0] + counts[1] + counts[2];
                    if (total > maxTotal) maxTotal = total;
                }
                if (maxTotal <= 0) maxTotal = 1;

                // Leyenda de colores
                int legendY = topMargin - 15;
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 8));
                int legendX = leftMargin;

                g2d.setColor(new Color(185, 28, 28)); // Rojo - Vencido
                g2d.fillRect(legendX, legendY - 8, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.drawString("Vencido", legendX + 14, legendY);

                legendX += 60;
                g2d.setColor(new Color(217, 119, 6)); // Naranja - Por vencer
                g2d.fillRect(legendX, legendY - 8, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.drawString("Por vencer", legendX + 14, legendY);

                legendX += 75;
                g2d.setColor(new Color(22, 101, 52)); // Verde - Vigente
                g2d.fillRect(legendX, legendY - 8, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.drawString("Vigente", legendX + 14, legendY);

                int y = topMargin;
                for (Map.Entry<String, int[]> entry : lifecycleCounts.entrySet()) {
                    String location = entry.getKey();
                    int[] counts = entry.getValue();
                    int vencido = counts[0];
                    int porVencer = counts[1];
                    int vigente = counts[2];
                    int total = vencido + porVencer + vigente;

                    if (total == 0) {
                        y += barHeight;
                        continue;
                    }

                    // Etiqueta de ubicación
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    String label = location.length() > 15 ? location.substring(0, 13) + "..." : location;
                    FontMetrics fm = g2d.getFontMetrics();
                    int textY = y + actualBarHeight / 2 + fm.getAscent() / 2;
                    g2d.drawString(label, 5, textY);

                    // Calcular anchos de cada segmento
                    int vencidoWidth = (int) ((vencido / (double) maxTotal) * chartWidth);
                    int porVencerWidth = (int) ((porVencer / (double) maxTotal) * chartWidth);
                    int vigenteWidth = (int) ((vigente / (double) maxTotal) * chartWidth);

                    int currentX = leftMargin;

                    // Segmento Vencido (Rojo)
                    if (vencido > 0) {
                        g2d.setColor(new Color(185, 28, 28));
                        g2d.fillRect(currentX, y, vencidoWidth, actualBarHeight);
                        g2d.setColor(new Color(55, 55, 80));
                        g2d.drawRect(currentX, y, vencidoWidth, actualBarHeight);

                        // Etiqueta de valor
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(new Font("Segoe UI", Font.BOLD, 8));
                        if (vencidoWidth > 20) {
                            g2d.drawString(String.valueOf(vencido), currentX + vencidoWidth / 2 - 3, textY);
                        }
                        currentX += vencidoWidth;
                    }

                    // Segmento Por vencer (Naranja)
                    if (porVencer > 0) {
                        g2d.setColor(new Color(217, 119, 6));
                        g2d.fillRect(currentX, y, porVencerWidth, actualBarHeight);
                        g2d.setColor(new Color(55, 55, 80));
                        g2d.drawRect(currentX, y, porVencerWidth, actualBarHeight);

                        // Etiqueta de valor
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(new Font("Segoe UI", Font.BOLD, 8));
                        if (porVencerWidth > 20) {
                            g2d.drawString(String.valueOf(porVencer), currentX + porVencerWidth / 2 - 3, textY);
                        }
                        currentX += porVencerWidth;
                    }

                    // Segmento Vigente (Verde)
                    if (vigente > 0) {
                        g2d.setColor(new Color(22, 101, 52));
                        g2d.fillRect(currentX, y, vigenteWidth, actualBarHeight);
                        g2d.setColor(new Color(55, 55, 80));
                        g2d.drawRect(currentX, y, vigenteWidth, actualBarHeight);

                        // Etiqueta de valor
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(new Font("Segoe UI", Font.BOLD, 8));
                        if (vigenteWidth > 20) {
                            g2d.drawString(String.valueOf(vigente), currentX + vigenteWidth / 2 - 3, textY);
                        }
                    }

                    // Total a la derecha
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    g2d.drawString("Total: " + total, leftMargin + chartWidth + 5, textY);

                    y += barHeight;
                }
            } else if (!data.isEmpty()) {
                // Fallback: mostrar barras horizontales originales
                int numBars = data.size();

                int topMargin = 25;
                int bottomMargin = 15;
                int usableHeight = height - topMargin - bottomMargin;
                int barHeight = usableHeight / Math.max(numBars, 1);

                int startX = 110;
                int rightMargin = 40;
                int chartWidth = Math.max(width - startX - rightMargin, 50);

                int maxValue = data.values().stream().max(Integer::compare).orElse(1);
                if (maxValue <= 0) maxValue = 1;

                Color[] colors = {new Color(70, 130, 180), new Color(220, 20, 60), new Color(34, 139, 34), new Color(255, 165, 0), new Color(123, 104, 238)};
                int colorIdx = 0;
                int y = topMargin;

                for (Map.Entry<String, Integer> entry : data.entrySet()) {
                    int value = entry.getValue();
                    int barW = (int) ((value / (double) maxValue) * chartWidth);

                    int currentBarHeight = Math.max(barHeight - 8, 4);

                    g2d.setColor(colors[colorIdx % colors.length]);
                    g2d.fillRect(startX, y + 4, barW, currentBarHeight);
                    g2d.setColor(new Color(55, 55, 80));
                    g2d.drawRect(startX, y + 4, barW, currentBarHeight);

                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    String label = entry.getKey();
                    if (label.length() > 18) label = label.substring(0, 16) + "...";

                    FontMetrics fm = g2d.getFontMetrics();
                    int textY = y + 4 + (currentBarHeight + fm.getAscent() - fm.getDescent()) / 2;
                    g2d.drawString(label, 5, textY);

                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2d.drawString(String.valueOf(value), startX + barW + 8, textY);

                    y += barHeight;
                    colorIdx++;
                }
            }
        }
    }

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Colores por defecto de fondo y texto (Tema Oscuro)
            if (isSelected) {
                c.setBackground(new Color(79, 70, 229));
                c.setForeground(Color.WHITE);
            } else {
                c.setBackground(new Color(30, 30, 46));
                c.setForeground(Color.WHITE);
            }
            
            // Columna 10 es "Alerta Ciclo Vida"
            if (column == 10) {
                String lifecycleAlert = value != null ? value.toString() : "";
                if ("Vencido".equalsIgnoreCase(lifecycleAlert)) {
                    c.setBackground(new Color(185, 28, 28));      // Rojo oscuro
                    c.setForeground(Color.WHITE);
                } else if ("Por vencer".equalsIgnoreCase(lifecycleAlert)) {
                    c.setBackground(new Color(217, 119, 6));      // Naranja oscuro
                    c.setForeground(Color.WHITE);
                } else if ("Vigente".equalsIgnoreCase(lifecycleAlert)) {
                    c.setBackground(new Color(22, 101, 52));      // Verde oscuro
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
            
            // Lógica para otras columnas
            String status = table.getValueAt(row, 4).toString();

            if (!isSelected) {
                if ("UNDER_MAINTENANCE".equalsIgnoreCase(status)) {
                    c.setBackground(new Color(120, 53, 4));  // Ámbar oscuro
                    c.setForeground(Color.WHITE);
                } else if ("RETIRED".equalsIgnoreCase(status) || "LOST".equalsIgnoreCase(status)) {
                    c.setBackground(new Color(55, 55, 70));   // Gris oscuro
                    c.setForeground(Color.LIGHT_GRAY);
                }
            }
            return c;
        }
    }
}
