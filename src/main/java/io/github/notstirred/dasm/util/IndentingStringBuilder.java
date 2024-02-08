package io.github.notstirred.dasm.util;

import java.util.Collections;

public class IndentingStringBuilder {
    private final StringBuilder builder = new StringBuilder();
    private final String indentation;
    private String currentIndentation;

    public IndentingStringBuilder(int indentationSize) {
        this.indentation = String.join("", Collections.nCopies(indentationSize, " "));
        this.currentIndentation = "";
    }

    public IndentingStringBuilder append(String string) {
        this.builder.append(this.currentIndentation).append(string);
        return this;
    }

    public IndentingStringBuilder appendLine(String string) {
        this.builder.append(this.currentIndentation).append(string).append("\n");
        return this;
    }

    public IndentingStringBuilder indent() {
        this.currentIndentation += this.indentation;
        return this;
    }

    public IndentingStringBuilder unindent() {
        this.currentIndentation = this.currentIndentation.substring(0, this.currentIndentation.length() - this.indentation.length());
        return this;
    }

    @Override public String toString() {
        return this.builder.toString();
    }
}
