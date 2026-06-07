package com.siat;

import com.siat.dao.ActivoDAO;
import com.siat.dao.CategoriaDAO;
import com.siat.models.Activo;
import com.siat.models.Usuario;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.time.format.DateTimeParseException;
import java.util.List;

public class EditAssetFrame extends JFrame {
    private final Usuario currentUser;
    private final Activo asset;
    private final AssetUpdateListener updateListener;

    private static final java.awt.Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    private final JTextField skuField = new RoundTextField(16);
    private final JTextField nameField = new RoundTextField(24);
    private final JComboBox<String> categoryBox = new JComboBox<>();
    private final JComboBox<String> locationBox = new JComboBox<>();
    private final JFormattedTextField purchaseDateField;
    private final JFormattedTextField decommissionDateField;
    private final JCheckBox maintenanceBox = new JCheckBox("Requiere mantenimiento");
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
    private final JFormattedTextField warrantyField;
    private final JTextField costField = new RoundTextField(10);
    private final JLabel messageLabel = new JLabel(" ");

    private java.util.List<String[]> categories;

    public EditAssetFrame(Usuario user, Activo asset, AssetUpdateListener listener) {
        this.currentUser = user;
        this.asset = asset;
        this.updateListener = listener;

        setTitle("Editar Activo");
        setSize(520, 480);
        setLocationRelativeTo(null);

        // Gradient Background
        GradientPanel mainPanel = new GradientPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setContentPane(mainPanel);

        // Form Card Panel
        RoundPanel card = new RoundPanel();
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        mainPanel.add(card, BorderLayout.CENTER);

        // Initialize masked date fields
        MaskFormatter dateMask = null;
        try {
            dateMask = new MaskFormatter("####-##-##");
            dateMask.setPlaceholderCharacter('_');
        } catch (Exception ex) {
            // fallback if MaskFormatter not available
        }
        purchaseDateField = dateMask != null ? new RoundFormattedTextField(dateMask) : new RoundFormattedTextField();
        decommissionDateField = dateMask != null ? new RoundFormattedTextField(dateMask) : new RoundFormattedTextField();
        warrantyField = dateMask != null ? new RoundFormattedTextField(dateMask) : new RoundFormattedTextField();

        purchaseDateField.setToolTipText("Formato YYYY-MM-DD (ej. 2024-05-10)");
        decommissionDateField.setToolTipText("Formato YYYY-MM-DD o dejar vacío");
        warrantyField.setToolTipText("Formato YYYY-MM-DD o dejar vacío");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;
        addField(card, gbc, y++, "SKU:", skuField);
        addField(card, gbc, y++, "Nombre:", nameField);
        addField(card, gbc, y++, "Categoría:", categoryBox);
        addField(card, gbc, y++, "Ubicación:", locationBox);
        addField(card, gbc, y++, "Fecha adquisición:", purchaseDateField);
        addField(card, gbc, y++, "Fecha baja:", decommissionDateField);
        addField(card, gbc, y++, "Garantía:", warrantyField);
        addField(card, gbc, y++, "Costo:", costField);
        addField(card, gbc, y++, "Cantidad:", quantitySpinner);

        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2;
        maintenanceBox.setBackground(new Color(30, 30, 46));
        maintenanceBox.setForeground(new Color(165, 180, 252));
        maintenanceBox.setFont(UI_FONT);
        card.add(maintenanceBox, gbc);

        gbc.gridy = y++;
        messageLabel.setFont(UI_FONT);
        messageLabel.setForeground(new Color(239, 68, 68));
        card.add(messageLabel, gbc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        
        ModernButton save = new ModernButton("Guardar Cambios");
        save.setPreferredSize(new Dimension(140, 32));
        save.addActionListener(e -> onSave());
        
        ModernButton cancel = new ModernButton("Cancelar");
        cancel.setPreferredSize(new Dimension(100, 32));
        cancel.addActionListener(e -> dispose());
        
        actions.add(save);
        actions.add(cancel);

        mainPanel.add(actions, BorderLayout.SOUTH);

        loadCategories();
        loadLocations();
        loadAssetData();
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UI_FONT);
        lbl.setForeground(new Color(165, 180, 252));
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        comp.setFont(UI_FONT);
        if (comp instanceof JTextField) {
            ((JTextField) comp).setColumns(16);
        } else if (comp instanceof JComboBox) {
            ((JComboBox<?>) comp).setPreferredSize(new Dimension(220, 26));
            comp.setBackground(new Color(20, 20, 32));
            comp.setForeground(Color.WHITE);
        } else if (comp instanceof JSpinner) {
            ((JSpinner) comp).setPreferredSize(new Dimension(100, 26));
            comp.setBackground(new Color(20, 20, 32));
            comp.setForeground(Color.WHITE);
            JComponent editor = ((JSpinner) comp).getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                ((JSpinner.DefaultEditor) editor).getTextField().setBackground(new Color(20, 20, 32));
                ((JSpinner.DefaultEditor) editor).getTextField().setForeground(Color.WHITE);
                ((JSpinner.DefaultEditor) editor).getTextField().setCaretColor(Color.WHITE);
            }
        }
        panel.add(comp, gbc);

        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
    }

    private void loadCategories() {
        categories = CategoriaDAO.list();
        categoryBox.removeAllItems();
        for (String[] pair : categories) {
            categoryBox.addItem(pair[1]);
        }
    }

    private void loadLocations() {
        locationBox.removeAllItems();
        String[] locations = {"Cuarto de Redes", "Sala de Servidores", "Almacén Central", "Bodega Alterna", "Centro de Acopio"};
        for (String location : locations) {
            locationBox.addItem(location);
        }
    }

    private void loadAssetData() {
        skuField.setText(asset.getSku());
        skuField.setEditable(false);
        nameField.setText(asset.getName());
        purchaseDateField.setText(asset.getPurchaseDate() != null ? asset.getPurchaseDate() : "");
        decommissionDateField.setText(asset.getDecommissionDate() != null ? asset.getDecommissionDate() : "");
        warrantyField.setText(asset.getWarrantyExpiry() != null ? asset.getWarrantyExpiry() : "");
        costField.setText(String.valueOf(asset.getCost()));
        quantitySpinner.setValue(asset.getQuantity());
        maintenanceBox.setSelected("YES".equals(asset.getMaintenance()));

        // Set category combobox
        boolean found = false;
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i)[1].equals(asset.getCategory())) {
                categoryBox.setSelectedIndex(i);
                found = true;
                break;
            }
        }
        if (!found && categories.size() > 0) {
            categoryBox.setSelectedIndex(0);
        }
        
        // Set location combobox
        locationBox.setSelectedItem(asset.getLocation());
    }

    private void onSave() {
        try {
            Activo a = new Activo();
            a.setSku(skuField.getText().trim());
            a.setName(nameField.getText().trim());
            int idx = categoryBox.getSelectedIndex();
            if (idx < 0) {
                messageLabel.setText("Seleccione una categoría");
                return;
            }
            a.setCategory(categories.get(idx)[0]);
            
            int locIdx = locationBox.getSelectedIndex();
            if (locIdx < 0) {
                messageLabel.setText("Seleccione una ubicación");
                return;
            }
            a.setLocation((String) locationBox.getSelectedItem());

            // Validate dates individually with specific messages
            String pd = purchaseDateField.getText().trim();
            if (!pd.isEmpty() && !pd.contains("_")) {
                try {
                    java.time.LocalDate.parse(pd);
                    a.setPurchaseDate(pd);
                } catch (DateTimeParseException ex) {
                    messageLabel.setText("Fecha adquisición inválida. Use YYYY-MM-DD");
                    return;
                }
            }

            String dd = decommissionDateField.getText().trim();
            if (!dd.isEmpty() && !dd.contains("_")) {
                try {
                    java.time.LocalDate.parse(dd);
                    a.setDecommissionDate(dd);
                } catch (DateTimeParseException ex) {
                    messageLabel.setText("Fecha baja inválida. Use YYYY-MM-DD");
                    return;
                }
            }

            String wy = warrantyField.getText().trim();
            if (!wy.isEmpty() && !wy.contains("_")) {
                try {
                    java.time.LocalDate.parse(wy);
                    a.setWarrantyExpiry(wy);
                } catch (DateTimeParseException ex) {
                    messageLabel.setText("Garantía inválida. Use YYYY-MM-DD");
                    return;
                }
            }

            a.setCost(costField.getText().isBlank() ? 0.0 : Double.parseDouble(costField.getText().trim()));
            a.setQuantity((Integer) quantitySpinner.getValue());
            a.setMaintenance(maintenanceBox.isSelected() ? "YES" : "NO");

            boolean ok = ActivoDAO.update(a, currentUser.getUserId());
            if (ok) {
                com.siat.dao.AuditDAO.log(currentUser.getUserId(), "ACTUALIZAR_ACTIVO", "assets", a.getSku(), true, "Activo actualizado: " + a.getName() + " (SKU: " + a.getSku() + ")");
                JOptionPane.showMessageDialog(this, "Activo actualizado correctamente.");
                if (updateListener != null) updateListener.onAssetSaved();
                dispose();
            } else {
                com.siat.dao.AuditDAO.log(currentUser.getUserId(), "ACTUALIZAR_ACTIVO", "assets", a.getSku(), false, "Fallo al actualizar activo: " + a.getName() + " (SKU: " + a.getSku() + ")");
                messageLabel.setText("Error al actualizar activo (revisar logs)");
            }
        } catch (NumberFormatException nfe) {
            messageLabel.setText("Formato de número inválido en costo o cantidad.");
        } catch (Exception e) {
            messageLabel.setText("Error: " + e.getMessage());
        }
    }
}
