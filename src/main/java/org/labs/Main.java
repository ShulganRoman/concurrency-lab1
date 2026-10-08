package org.labs;

import org.labs.domain.Programmer;
import org.labs.domain.Table;
import org.labs.service.ConfigLoader;

import java.io.IOException;
import java.util.Properties;

public class Main {
    static void main() {
        Table table;

        try {
            Properties props = ConfigLoader.loadProps();
            table = new Table(
                    Integer.parseInt(props.getProperty("amount_around_table")),
                    Integer.parseInt(props.getProperty("waiters")),
                    Integer.parseInt(props.getProperty("dishes")));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Некорректная конфигурация: " + e.getMessage());
            System.exit(1);
            return;
        }

        table.startEating();

        for (var p : table.getProgrammers())
            System.out.println(p.getEaten());

        System.out.println("total: " + table.getProgrammers().stream().mapToInt(Programmer::getEaten).sum() + " dishes eaten");
    }
}
