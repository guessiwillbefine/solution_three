package parser;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ViolationDTO {
    @JsonProperty("type")
    private String type;
    @JsonProperty("amount")
    private double amount;

    public ViolationDTO(String type, double amount) {
        this.type = type;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "ViolationDTO{" +
                "type='" + type + '\'' +
                ", amount=" + amount +
                '}';
    }
}
