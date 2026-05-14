package com.cr.back.rules.facts;

public class PlayerInsight {
    private final String code;
    private final String explanation;

    public PlayerInsight(String code, String explanation) {
        this.code = code;
        this.explanation = explanation;
    }

    public String getCode() {
        return code;
    }

    public String code() {
        return code;
    }

    public String getExplanation() {
        return explanation;
    }

    public String explanation() {
        return explanation;
    }
}
