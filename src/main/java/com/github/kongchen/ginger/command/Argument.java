package com.github.kongchen.ginger.command;

/**
 * Created by chekong on 13-6-18.
 */
public class Argument {
    public Argument(ArgType currentType, String value) {
        this.type = currentType;
        if (currentType == ArgType.HEADER_VALUE || currentType == ArgType.BODY_VALUE) {
            this.value = value.substring(1);
        } else {
            this.value = value;
        }
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setType(ArgType type) {
        this.type = type;
    }

    public enum ArgType {STRING_VALUE, HEADER_VALUE, BODY_VALUE, UNKNOWN_VALUE}

    ;

    private ArgType type;

    private String value;

    public String getValue() {
        return value;
    }

    public ArgType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Argument{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
