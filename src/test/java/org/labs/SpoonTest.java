package org.labs;

import org.junit.jupiter.api.Test;
import org.labs.domain.IllegalStateOfSpoonException;
import org.labs.domain.Spoon;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpoonTest {

    @Test
    void newSpoonIsOnTable() {
        Spoon spoon = new Spoon();

        assertTrue(spoon.isOnTable());
    }

    @Test
    void takeSpoonAndPutSpoonToggleState() {
        Spoon spoon = new Spoon();

        spoon.takeSpoon();
        assertFalse(spoon.isOnTable());

        spoon.putSpoon();
        assertTrue(spoon.isOnTable());
    }

    @Test
    void takingTakenSpoonFails() {
        Spoon spoon = new Spoon();
        spoon.takeSpoon();

        assertThrows(IllegalStateOfSpoonException.class, spoon::takeSpoon);
    }

    @Test
    void puttingSpoonThatIsOnTableFails() {
        Spoon spoon = new Spoon();

        assertThrows(IllegalStateOfSpoonException.class, spoon::putSpoon);
    }
}
