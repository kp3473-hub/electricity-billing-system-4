import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Represents a single utility bill for a customer, including usage and financial breakdown.
 */
public class Bill implements Serializable {
    // Identity and relationship
    private final UUID id;
    private final UUID customerId;
    private final String customerSnapshot; // Details like name, address at time of billing
    private final String meterSnapshot;    // Details like meter ID, type
    
    // Usage and period
    private final YearMonth month;
    private final int units; // units = currentReading - previousReading

    // Rates and charges (pulled from a Rate/Tariff table in a real system)
    private final double ratePerUnit;
    private final double fixedCharge;
    private final double taxPercent; // e.g., 10.0 for 10%
    
    // Financial breakdown
    private final double subtotal;
    private final double tax;
    private final double total;

    // Status and dates
    private BillStatus status;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private LocalDate paidDate;

    public enum BillStatus {
        DRAFT,
        ISSUED,
        PAID,
        OVERDUE,
        CANCELLED
    }

    // --- Constructor (Used by the service) ---
    public Bill(UUID customerId, String customerSnapshot, String meterSnapshot, 
                YearMonth month, int units, double ratePerUnit, 
                double fixedCharge, double taxPercent, LocalDate issueDate, LocalDate dueDate) {
        
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.customerSnapshot = customerSnapshot;
        this.meterSnapshot = meterSnapshot;
        this.month = month;
        this.units = units;
        this.ratePerUnit = ratePerUnit;
        this.fixedCharge = fixedCharge;
        this.taxPercent = taxPercent;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = BillStatus.ISSUED; // Default status upon creation

        // Calculation methods (called within constructor)
        this.subtotal = calculateSubtotal();
        this.tax = calculateTax();
        this.total = this.subtotal + this.tax;
    }

    /**
     * Calculates the subtotal: (Units * Rate) + Fixed Charge.
     * @return The pre-tax subtotal.
     */
    private double calculateSubtotal() {
        return (this.units * this.ratePerUnit) + this.fixedCharge;
    }

    /**
     * Calculates the tax amount: Subtotal * (Tax Percent / 100).
     * @return The tax amount.
     */
    private double calculateTax() {
        return this.subtotal * (this.taxPercent / 100.0);
    }
    
    // --- Getters (to resolve "Variable is never read" warnings) ---

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getCustomerSnapshot() {
        return customerSnapshot;
    }

    public String getMeterSnapshot() {
        return meterSnapshot;
    }

    public YearMonth getMonth() {
        return month;
    }

    public int getUnits() {
        return units;
    }

    public double getRatePerUnit() {
        return ratePerUnit;
    }

    public double getFixedCharge() {
        return fixedCharge;
    }

    public double getTaxPercent() {
        return taxPercent;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getTax() {
        return tax;
    }

    public double getTotal() {
        return total;
    }

    public BillStatus getStatus() {
        return status;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    // --- Setters for updateable fields (e.g., when a payment is processed) ---

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }

    public void setStatus(BillStatus status) {
        this.status = status;
    }
}
