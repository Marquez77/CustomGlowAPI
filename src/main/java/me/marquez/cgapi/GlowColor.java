package me.marquez.cgapi;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GlowColor {
    BLACK("BLACK", '0'),
    DARK_BLUE("DARK_BLUE", '1'),
    DARK_GREEN("DARK_GREEN", '2'),
    DARK_AQUA("DARK_AQUA", '3'),
    DARK_RED("DARK_RED", '4'),
    DARK_PURPLE("DARK_PURPLE", '5'),
    GOLD("GOLD", '6'),
    GRAY("GRAY", '7'),
    DARK_GRAY("DARK_GRAY", '8'),
    BLUE("BLUE", '9'),
    GREEN("GREEN", 'a'),
    AQUA("AQUA", 'b'),
    RED("RED", 'c'),
    LIGHT_PURPLE("LIGHT_PURPLE", 'd'),
    YELLOW("YELLOW", 'e'),
    WHITE("WHITE", 'f'),
    OBFUSCATED("OBFUSCATED", 'k'),
    BOLD("BOLD", 'l'),
    STRIKETHROUGH("STRIKETHROUGH", 'm'),
    UNDERLINE("UNDERLINE", 'n'),
    ITALIC("ITALIC", 'o'),
    RESET("RESET", 'r');

    private final String name;
    private final char code;
}
