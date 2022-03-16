package nl.digitalekabeltelevisie.util.CLI.beans;

public class RangeData {

    private Long byteValue;
    private Integer displayedValue;
    private String unit;

    public RangeData(Long byteValue, Integer displayedValue, String unit) {
        this.byteValue = byteValue;
        this.displayedValue = displayedValue;
        this.unit = unit;
    }

    public Long getByteValue() {
        return byteValue;
    }

    public Integer getDisplayedValue() {
        return displayedValue;
    }

    public String getUnit() {
        return unit;
    }
}