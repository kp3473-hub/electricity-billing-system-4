package com.ebilling.frontend.ui;

// Customer model is provided locally below instead of importing a missing backend package
// BillingService backend package is not available in this frontend; a minimal local interface
// is defined at the end of this file to satisfy compile-time references.

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.UUID;

public class CustomerPanel extends JPanel {
    private final BillingService service;
    private final Runnable onChange;

    private final JTable table = new JTable();
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Meter", "Address"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private final JTextField txtName = new JTextField();
    private final JTextField txtMeter = new JTextField();
    private final JTextArea txtAddress = new JTextArea(3, 20);

    public CustomerPanel(BillingService service, Runnable onChange) {
        this.service = service;
        this.onChange = onChange;
        setLayout(new BorderLayout(10,10));
        setBorder(new EmptyBorder(10,10,10,10));

        add(buildForm(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        reloadTable();
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Customer Details"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        gc.gridx=0; gc.gridy=row; p.add(new JLabel("Name"), gc);
        gc.gridx=1; gc.gridy=row; p.add(txtName, gc);

        row++;
        gc.gridx=0; gc.gridy=row; p.add(new JLabel("Meter No."), gc);
        gc.gridx=1; gc.gridy=row; p.add(txtMeter, gc);

        row++;
        gc.gridx=0; gc.gridy=row; p.add(new JLabel("Address"), gc);
        JScrollPane area = new JScrollPane(txtAddress);
        gc.gridx=1; gc.gridy=row; p.add(area, gc);

        return p;
    }

    private JScrollPane buildTable() {
        table.setModel(model);
        // Hide ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UIUtils.makeTablePretty(table);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    txtName.setText(String.valueOf(model.getValueAt(table.convertRowIndexToModel(row), 1)));
                    txtMeter.setText(String.valueOf(model.getValueAt(table.convertRowIndexToModel(row), 2)));
                    txtAddress.setText(String.valueOf(model.getValueAt(table.convertRowIndexToModel(row), 3)));
                }
            }
        });
        return new JScrollPane(table);
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnNew = UIUtils.lightButton("➕ New");
        JButton btnSave = UIUtils.primaryButton("💾 Save");
        JButton btnDelete = UIUtils.lightButton("🗑 Delete");
        JButton btnClear = UIUtils.lightButton("🧹 Clear");

        p.add(btnClear);
        p.add(btnDelete);
        p.add(btnSave);
        p.add(btnNew);

        btnNew.addActionListener(e -> clearForm());
        btnClear.addActionListener(e -> clearForm());
        btnSave.addActionListener(e -> onSave());
        btnDelete.addActionListener(e -> onDelete());

        return p;
    }

    private void onSave() {
        String name = txtName.getText().trim();
        String meter = txtMeter.getText().trim();
        String addr = txtAddress.getText().trim();
        if (name.isEmpty() || meter.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Meter No. are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int sel = table.getSelectedRow();
        if (sel >= 0) {
            int modelRow = table.convertRowIndexToModel(sel);
            UUID id = UUID.fromString(String.valueOf(model.getValueAt(modelRow, 0)));
            service.updateCustomer(id, name, meter, addr);
            JOptionPane.showMessageDialog(this, "Customer updated.");
        } else {
            service.addCustomer(name, meter, addr);
            JOptionPane.showMessageDialog(this, "Customer added.");
        }
        reloadTable();
        clearForm();
        onChange.run();
    }

    private void onDelete() {
        int sel = table.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Select a customer to delete.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(sel);
        UUID id = UUID.fromString(String.valueOf(model.getValueAt(modelRow, 0)));

        int res = JOptionPane.showConfirmDialog(this, "Delete this customer?\nYou can also delete their bills.", "Confirm", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            int res2 = JOptionPane.showConfirmDialog(this, "Also delete bills for this customer?\nYes = delete bills, No = keep bills", "Delete Bills?", JOptionPane.YES_NO_OPTION);
            boolean removeBills = res2 == JOptionPane.YES_OPTION;
            if (service.deleteCustomer(id, removeBills)) {
                JOptionPane.showMessageDialog(this, "Customer deleted" + (removeBills ? " (and bills removed)" : ""));
                reloadTable();
                onChange.run();
            }
        }
    }

    public final void reloadTable() {
        List<Customer> customers = service.getCustomers();
        model.setRowCount(0);
        for (Customer c : customers) {
            model.addRow(new Object[]{ c.id.toString(), c.name, c.meterNumber, c.address == null ? "" : c.address });
        }
    }

    private void clearForm() {
        table.clearSelection();
        txtName.setText("");
        txtMeter.setText("");
        txtAddress.setText("");
    }
    // Minimal local Customer model to satisfy compile-time references in this UI.
    // Fields are public for direct access consistent with how the panel reads them.
    public static class Customer {
        public java.util.UUID id;
        public String name;
        public String meterNumber;
        public String address;

        public Customer(java.util.UUID id, String name, String meterNumber, String address) {
            this.id = id;
            this.name = name;
            this.meterNumber = meterNumber;
            this.address = address;
        }
    }

    // Minimal local BillingService interface nested into CustomerPanel so the public
    // CustomerPanel API doesn't expose a package-private top-level type.
    public static interface BillingService {
        java.util.List<Customer> getCustomers();
        void addCustomer(String name, String meterNumber, String address);
        void updateCustomer(java.util.UUID id, String name, String meterNumber, String address);
        boolean deleteCustomer(java.util.UUID id, boolean removeBills);
    }
}

// Minimal local UI utilities used by the panel when the original UIUtils is not available.
final class UIUtils {
    static JButton lightButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(new java.awt.Color(230,230,230));
        return b;
    }
    static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(new java.awt.Color(70,130,180));
        b.setForeground(Color.WHITE);
        return b;
    }

    static void makeTablePretty(JTable table) {
        // Basic visual tweaks to make the table look nicer
        table.setRowHeight(22);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoCreateRowSorter(true);
        table.setSelectionBackground(new Color(184, 207, 229));
        table.setSelectionForeground(Color.BLACK);
    }
}