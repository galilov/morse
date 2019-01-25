package ru.otus.java32;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: add a text line to send");
            return;
        }
        MorseProcessor mp = new MorseProcessor();
        String morse = mp.textToMorse(String.join(" ", args));
        Transmitter transmitter = new Transmitter();
        transmitter.transmit(morse);
    }
}
