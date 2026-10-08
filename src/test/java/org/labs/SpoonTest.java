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

        assertFalse(spoon.getInUse());
    }

    @Test
    void takeSpoonAndPutSpoonToggleState() {
        Spoon spoon = new Spoon();

        spoon.tryTakeSpoon();
        assertTrue(spoon.getInUse());

        spoon.putSpoon();
        assertFalse(spoon.getInUse());
    }

    @Test
    void puttingSpoonThatIsOnTableFails() {
        Spoon spoon = new Spoon();

        assertThrows(IllegalStateOfSpoonException.class, spoon::putSpoon);
    }
}
