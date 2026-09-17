package org.labs;

import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        int programmers = 0;
        int waiters = 0;
        int dishes = 0;

        try {
            Properties props = ConfigLoader.loadProps();
            programmers = Integer.parseInt(props.getProperty("amount_around_table"));
            waiters = Integer.parseInt(props.getProperty("waiters"));
            dishes = Integer.parseInt(props.getProperty("dishes"));
        } catch (IOException | NumberFormatException e) {
            System.err.println("Не удалось загрузить конфигурацию: " + e.getMessage());
            System.exit(1);
        }

        if (programmers < 2 || waiters < 1 || dishes < 1) {
            System.err.println("Некорректная конфигурация: programmers=" + programmers
                    + ", waiters=" + waiters + ", dishes=" + dishes);
            System.exit(1);
        }

        Table table = new Table(programmers, waiters, dishes);
        table.run();

        for(var p : table.getInfo())
            System.out.println(p.getEaten());

        System.out.println("total: " + table.getInfo().stream().mapToInt(i -> i.getEaten().get()).sum() + " dishes eaten");
    }
}