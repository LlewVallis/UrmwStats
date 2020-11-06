package org.astropeci.urmwstats.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum HelpSection {

    GLOBAL(null),
    UTILITY("Utility commands"),
    RECORDING("Recording commands"),
    TEMPLATE("Template commands"),
    MISC("Miscellaneous commands");

    @Getter
    private final String title;
}
