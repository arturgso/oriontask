package br.com.oriontask.backend.utils;

public class ColorGenerator {
  public static String generateRandomColor() {
    int r = (int) (Math.random() * 256);
    int g = (int) (Math.random() * 256);
    int b = (int) (Math.random() * 256);
    return String.format("#%02X%02X%02X", r, g, b);
  }
}
